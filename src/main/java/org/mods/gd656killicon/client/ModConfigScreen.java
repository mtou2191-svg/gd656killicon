package org.mods.gd656killicon.client;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.mods.gd656killicon.Config;
import org.mods.gd656killicon.network.ScoreRequestPacket;

public class ModConfigScreen extends Screen {
   private final Screen parent;
   private static final int BUTTON_HEIGHT = 20;
   private static final int BUTTON_WIDTH = 80;
   private static final int MAX_UNDO_HISTORY = 10;
   private static final int MENU_BUTTON_SIZE = 20;
   private static final int MODE_BUTTON_HEIGHT = 20;
   private static final int MODE_BUTTON_WIDTH = 60;
   private static final int PADDING = 10;
   private static final int TITLE_HEIGHT = 20;
   private static final long RESET_CONFIRMATION_TIMEOUT = 3000L;
   private static final long SCORE_UPDATE_INTERVAL = 5000L;
   private final List<ModConfigScreen.CustomButton> customMenuButtons = new ArrayList<>();
   private final List<ModConfigScreen.DraggableElement> draggableElements = new ArrayList<>();
   private final ButtonWidget[] modeButtons = new ButtonWidget[3];
   private final List<ModConfigScreen.MoveOperation> undoHistory = new ArrayList<>();
   private ButtonWidget menuButton;
   private ButtonWidget resetButton;
   private ButtonWidget undoButton;
   private ModConfigScreen.DraggableElement selectedElement = null;
   private ModConfigScreen.EditMode currentEditMode = ModConfigScreen.EditMode.SCROLL;
   private int currentScore = 0;
   private boolean canUndo = false;
   private boolean isMenuExpanded = false;
   private boolean resetConfirmation = false;
   private long lastScoreUpdateTime = 0L;
   private long resetConfirmationTime = 0L;

   public ModConfigScreen(Screen parent) {
      super(Text.translatable("gd656killicon.modconfig.title"));
      this.parent = parent;
   }

   protected void init() {
      super.init();
      this.requestScoreUpdate();
      this.initializeDraggableElements();
      this.menuButton = ButtonWidget.builder(Text.literal("≡"), button -> this.toggleMenu()).dimensions(10, 10, 20, 20).build();
      this.addDrawableChild(this.menuButton);
      this.updateScoreText();
      this.addDrawableChild(ButtonWidget.builder(Text.translatable("gd656killicon.modconfig.details"), button -> {
         if (this.client != null) {
            this.client.setScreen(new ConfigScreen(this));
         }
      }).dimensions(this.width - 80 - 10, 10, 80, 20).build());
      this.initCustomMenuButtons();
      int modeButtonsStartX = this.width - 60 - 10;
      int modeButtonsStartY = 40;
      this.modeButtons[0] = ButtonWidget.builder(Text.literal("滚动"), button -> this.switchEditMode(ModConfigScreen.EditMode.SCROLL))
         .dimensions(modeButtonsStartX, modeButtonsStartY, 60, 20)
         .build();
      this.modeButtons[1] = ButtonWidget.builder(Text.literal("连杀"), button -> this.switchEditMode(ModConfigScreen.EditMode.COMBO))
         .dimensions(modeButtonsStartX, modeButtonsStartY + 20 + 5, 60, 20)
         .build();
      this.modeButtons[2] = ButtonWidget.builder(Text.literal("卡牌"), button -> this.switchEditMode(ModConfigScreen.EditMode.CARD))
         .dimensions(modeButtonsStartX, modeButtonsStartY + 50, 60, 20)
         .build();
      this.updateModeButtons();

      for (ButtonWidget button : this.modeButtons) {
         this.addDrawableChild(button);
      }

      int bottomButtonY = this.height - 20 - 10;
      int buttonSpacing = 10;
      int totalButtonsWidth = 240 + buttonSpacing * 2;
      int buttonsStartX = this.width - totalButtonsWidth - 10;
      this.resetButton = ButtonWidget.builder(Text.translatable("gd656killicon.modconfig.reset"), button -> this.handleResetButton())
         .dimensions(buttonsStartX, bottomButtonY, 80, 20)
         .build();
      this.addDrawableChild(this.resetButton);
      this.undoButton = ButtonWidget.builder(Text.translatable("gd656killicon.modconfig.undo"), button -> this.handleUndoButton())
         .dimensions(buttonsStartX + 80 + buttonSpacing, bottomButtonY, 80, 20)
         .build();
      this.addDrawableChild(this.undoButton);
      this.updateUndoButtonState();
      this.addDrawableChild(ButtonWidget.builder(Text.translatable("gd656killicon.modconfig.save"), button -> {
         this.savePositionsToConfig();
         if (this.client != null) {
            this.client.setScreen(this.parent);
         }
      }).dimensions(buttonsStartX + (80 + buttonSpacing) * 2, bottomButtonY, 80, 20).build());
   }

   public void close() {
      this.savePositionsToConfig();
      if (this.client != null) {
         this.client.setScreen(this.parent);
      }
   }

   public void render(@NotNull DrawContext guiGraphics, int mouseX, int mouseY, float partialTick) {
      if (System.currentTimeMillis() - this.lastScoreUpdateTime > 5000L) {
         this.requestScoreUpdate();
      }

      if (this.resetConfirmation && System.currentTimeMillis() - this.resetConfirmationTime > 3000L) {
         this.resetConfirmation = false;
         this.resetButton.setMessage(Text.translatable("gd656killicon.modconfig.reset"));
      }

      guiGraphics.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, -1);
      String modeText = "当前模式: " + this.currentEditMode.displayName;
      guiGraphics.drawText(
         this.textRenderer, modeText, this.width / 2 - this.textRenderer.getWidth(modeText) / 2, 40, this.currentEditMode.color, true
      );
      Text label = Text.literal("您在此服务器的总分数: ").styled(style -> style.withColor(-1));
      String scoreText = String.valueOf(this.currentScore);
      int labelX = 40;
      int scoreX = labelX + this.textRenderer.getWidth(label) + 5;
      guiGraphics.drawText(this.textRenderer, label, labelX, 16, -1, true);
      guiGraphics.drawText(
         this.textRenderer, Text.literal(scoreText).styled(style -> style.withColor(-65536).withBold(true)), scoreX, 16, -65536, true
      );
      if (this.isMenuExpanded) {
         this.renderCustomButtons(guiGraphics, mouseX, mouseY);
      }

      this.renderDraggableElements(guiGraphics, mouseX, mouseY);
      String versionText = "v1.0.0 公测版 RC5 *Fabric*";
      guiGraphics.drawText(this.textRenderer, versionText, 10, this.height - 9 - 10, 8947848, true);
      String instruction = "左键拖动调整位置 | 右键点击配置元素";
      guiGraphics.drawText(this.textRenderer, instruction, 10, this.height - 9 * 2 - 10 - 5, 13421772, true);
      super.render(guiGraphics, mouseX, mouseY, partialTick);
   }

   public boolean mouseClicked(Click click, boolean doubled) {
      double mouseX = click.x();
      double mouseY = click.y();
      int button = click.button();
      if (this.isMenuExpanded && button == 0) {
         boolean clickedOnButton = false;

         for (ModConfigScreen.CustomButton customButton : this.customMenuButtons) {
            if (customButton.contains((int)mouseX, (int)mouseY)) {
               customButton.onClick.run();
               clickedOnButton = true;
               break;
            }
         }

         if (!clickedOnButton) {
            boolean clickedMenuButton = mouseX >= 10.0 && mouseX <= 30.0 && mouseY >= 10.0 && mouseY <= 30.0;
            if (!clickedMenuButton) {
               this.toggleMenu();
               return true;
            }
         }

         if (clickedOnButton) {
            return true;
         }
      }

      if (button == 1) {
         for (ModConfigScreen.DraggableElement element : this.draggableElements) {
            if (element.contains((int)mouseX, (int)mouseY, this.width, this.height)) {
               if (this.client != null) {
                  this.client.setScreen(new ElementConfigScreen(this, element));
               }

               return true;
            }
         }
      }

      if (button == 0) {
         for (ModConfigScreen.DraggableElement elementx : this.draggableElements) {
            if (elementx.contains((int)mouseX, (int)mouseY, this.width, this.height)) {
               this.selectedElement = elementx;
               int oldX = elementx.x;
               int oldY = elementx.y;
               elementx.setDragOffset((int)mouseX, (int)mouseY, this.width, this.height);
               this.selectedElement.setUserData(new int[]{oldX, oldY});
               return true;
            }
         }

         this.selectedElement = null;
      }

      return super.mouseClicked(click, doubled);
   }

   public boolean mouseDragged(Click click, double dragX, double dragY) {
      if (this.selectedElement != null) {
         this.selectedElement.setPositionFromMouse((int)click.x(), (int)click.y(), this.width, this.height);
         return true;
      } else {
         return super.mouseDragged(click, dragX, dragY);
      }
   }

   public boolean mouseReleased(Click click) {
      if (this.selectedElement != null) {
         int[] oldPos = (int[])this.selectedElement.getUserData();
         if (oldPos != null) {
            int oldX = oldPos[0];
            int oldY = oldPos[1];
            int newX = this.selectedElement.x;
            int newY = this.selectedElement.y;
            this.recordMoveOperation(this.selectedElement.type, oldX, oldY, newX, newY);
            this.selectedElement.setUserData(null);
         }

         this.savePositionsToConfig();
         this.selectedElement = null;
         return true;
      } else {
         return super.mouseReleased(click);
      }
   }

   public void updateCurrentScore(int score) {
      this.currentScore = score;
   }

   private void clearUndoHistory() {
      this.undoHistory.clear();
      this.updateUndoButtonState();
   }

   private int getCurrentPosition(ModConfigScreen.ElementType type, boolean isX) {
      return switch (this.currentEditMode) {
         case SCROLL -> this.getScrollPosition(type, isX);
         case COMBO -> this.getComboPosition(type, isX);
         case CARD -> this.getCardPosition(type, isX);
      };
   }

   private ModConfigScreen.EditMode getEditModeForIndex(int index) {
      return switch (index) {
         case 1 -> ModConfigScreen.EditMode.COMBO;
         case 2 -> ModConfigScreen.EditMode.CARD;
         default -> ModConfigScreen.EditMode.SCROLL;
      };
   }

   private int getElementSize(ModConfigScreen.ElementType type) {
      return switch (this.currentEditMode) {
         case SCROLL -> {
            switch (type) {
               case BONUS:
                  yield Config.scrollBonusSize;
               case BOTTOMBAR:
                  yield 100;
               case ICON:
                  yield Config.scrollIconSize;
               case SCORE:
                  yield Config.scrollScoreSize;
               case SUBTITLE:
                  yield Config.scrollSubtitleSize;
               default:
                  throw new IncompatibleClassChangeError();
            }
         }
         case COMBO -> {
            switch (type) {
               case BONUS:
                  yield Config.comboBonusSize;
               case BOTTOMBAR:
                  yield 100;
               case ICON:
                  yield Config.comboIconSize;
               case SCORE:
                  yield Config.comboScoreSize;
               case SUBTITLE:
                  yield Config.comboSubtitleSize;
               default:
                  throw new IncompatibleClassChangeError();
            }
         }
         case CARD -> {
            switch (type) {
               case BONUS:
                  yield Config.cardBonusSize;
               case BOTTOMBAR:
                  yield Config.cardBottombarSize;
               case ICON:
                  yield Config.cardIconSize;
               case SCORE:
                  yield Config.cardScoreSize;
               case SUBTITLE:
                  yield Config.cardSubtitleSize;
               default:
                  throw new IncompatibleClassChangeError();
            }
         }
      };
   }

   private boolean getElementVisible(ModConfigScreen.ElementType type) {
      return switch (this.currentEditMode) {
         case SCROLL -> {
            switch (type) {
               case BONUS:
                  yield Config.scrollBonusVisible;
               case BOTTOMBAR:
                  yield false;
               case ICON:
                  yield Config.scrollIconVisible;
               case SCORE:
                  yield Config.scrollScoreVisible;
               case SUBTITLE:
                  yield Config.scrollSubtitleVisible;
               default:
                  throw new IncompatibleClassChangeError();
            }
         }
         case COMBO -> {
            switch (type) {
               case BONUS:
                  yield Config.comboBonusVisible;
               case BOTTOMBAR:
                  yield false;
               case ICON:
                  yield Config.comboIconVisible;
               case SCORE:
                  yield Config.comboScoreVisible;
               case SUBTITLE:
                  yield Config.comboSubtitleVisible;
               default:
                  throw new IncompatibleClassChangeError();
            }
         }
         case CARD -> {
            switch (type) {
               case BONUS:
                  yield Config.cardBonusVisible;
               case BOTTOMBAR:
                  yield Config.cardBottombarVisible;
               case ICON:
                  yield Config.cardIconVisible;
               case SCORE:
                  yield Config.cardScoreVisible;
               case SUBTITLE:
                  yield Config.cardSubtitleVisible;
               default:
                  throw new IncompatibleClassChangeError();
            }
         }
      };
   }

   private void handleCustomMenuButtonClick(String buttonLabel) {
      MenuManager menuManager = MenuManager.getInstance();
      switch (buttonLabel) {
         case "排行榜":
            menuManager.openRankingList(this);
            break;
         case "历史记录":
            menuManager.openHistoryRecord(this);
            break;
         case "设置":
            menuManager.openMoreSettings(this);
            break;
         case "帮助":
            menuManager.openHelp(this);
            break;
         case "关于":
            menuManager.openAbout(this);
            break;
         case "反馈":
            menuManager.openFeedback(this);
      }

      this.toggleMenu();
   }

   private void handleResetButton() {
      if (this.resetConfirmation) {
         Config.resetAllToDefaults();
         this.clearUndoHistory();
         this.initializeDraggableElements();
         this.resetConfirmation = false;
         this.resetButton.setMessage(Text.translatable("gd656killicon.modconfig.reset"));
      } else {
         this.resetConfirmation = true;
         this.resetConfirmationTime = System.currentTimeMillis();
         this.resetButton.setMessage(Text.translatable("gd656killicon.modconfig.reset_confirm"));
      }
   }

   private void handleUndoButton() {
      if (this.canUndo && !this.undoHistory.isEmpty()) {
         this.undoLastMove();
      }
   }

   private void initCustomMenuButtons() {
      this.customMenuButtons.clear();
      int buttonY = 40;
      int buttonHeight = 20;
      int buttonWidth = buttonHeight * 5;
      String[] buttonLabels = new String[]{"排行榜", "历史记录", "设置", "帮助", "关于", "反馈"};

      for (int i = 0; i < buttonLabels.length; i++) {
         int currentY = buttonY + i * buttonHeight;
         int finalI = i;
         ModConfigScreen.CustomButton button = new ModConfigScreen.CustomButton(
            buttonLabels[finalI], 10, currentY, buttonWidth, buttonHeight, () -> this.handleCustomMenuButtonClick(buttonLabels[finalI])
         );
         this.customMenuButtons.add(button);
      }
   }

   private void initializeDraggableElements() {
      this.draggableElements.clear();
      int iconBaseWidth = 70;
      int iconBaseHeight = 70;
      int subtitleBaseWidth = 200;
      int subtitleBaseHeight = 40;
      int scoreBaseWidth = 60;
      int scoreBaseHeight = 40;
      int bonusBaseWidth = 60;
      int bonusBaseHeight = 50;
      int iconConfigSize = this.getElementSize(ModConfigScreen.ElementType.ICON);
      int subtitleConfigSize = this.getElementSize(ModConfigScreen.ElementType.SUBTITLE);
      int scoreConfigSize = this.getElementSize(ModConfigScreen.ElementType.SCORE);
      int bonusConfigSize = this.getElementSize(ModConfigScreen.ElementType.BONUS);
      float iconScale = iconConfigSize / 32.0F;
      float subtitleScale = subtitleConfigSize / 100.0F;
      float scoreScale = scoreConfigSize / 250.0F;
      float bonusScale = bonusConfigSize / 100.0F;
      int iconWidth = (int)(iconBaseWidth * iconScale);
      int iconHeight = (int)(iconBaseHeight * iconScale);
      int subtitleWidth = (int)(subtitleBaseWidth * subtitleScale);
      int subtitleHeight = (int)(subtitleBaseHeight * subtitleScale);
      int scoreWidth = (int)(scoreBaseWidth * scoreScale);
      int scoreHeight = (int)(scoreBaseHeight * scoreScale);
      int bonusWidth = (int)(bonusBaseWidth * bonusScale);
      int bonusHeight = (int)(bonusBaseHeight * bonusScale);
      this.draggableElements
         .add(
            new ModConfigScreen.DraggableElement(
               "击杀图标",
               ModConfigScreen.ElementType.ICON,
               this.getCurrentPosition(ModConfigScreen.ElementType.ICON, true),
               this.getCurrentPosition(ModConfigScreen.ElementType.ICON, false),
               iconWidth,
               iconHeight,
               true
            )
         );
      this.draggableElements
         .add(
            new ModConfigScreen.DraggableElement(
               "击败信息字幕",
               ModConfigScreen.ElementType.SUBTITLE,
               this.getCurrentPosition(ModConfigScreen.ElementType.SUBTITLE, true),
               this.getCurrentPosition(ModConfigScreen.ElementType.SUBTITLE, false),
               subtitleWidth,
               subtitleHeight,
               true
            )
         );
      this.draggableElements
         .add(
            new ModConfigScreen.DraggableElement(
               "分数字幕",
               ModConfigScreen.ElementType.SCORE,
               this.getCurrentPosition(ModConfigScreen.ElementType.SCORE, true),
               this.getCurrentPosition(ModConfigScreen.ElementType.SCORE, false),
               scoreWidth,
               scoreHeight,
               true
            )
         );
      this.draggableElements
         .add(
            new ModConfigScreen.DraggableElement(
               "加分项字幕",
               ModConfigScreen.ElementType.BONUS,
               this.getCurrentPosition(ModConfigScreen.ElementType.BONUS, true),
               this.getCurrentPosition(ModConfigScreen.ElementType.BONUS, false),
               bonusWidth,
               bonusHeight,
               true
            )
         );
      switch (Config.iconMode) {
         case SCROLLING:
            this.currentEditMode = ModConfigScreen.EditMode.SCROLL;
            break;
         case COMBO:
            this.currentEditMode = ModConfigScreen.EditMode.COMBO;
            break;
         case CARD:
            this.currentEditMode = ModConfigScreen.EditMode.CARD;
      }

      this.loadPositionsFromConfig();
   }

   private void loadPositionsFromConfig() {
      for (ModConfigScreen.DraggableElement element : this.draggableElements) {
         switch (this.currentEditMode) {
            case SCROLL:
               element.x = this.getScrollPosition(element.type, true);
               element.y = this.getScrollPosition(element.type, false);
               break;
            case COMBO:
               element.x = this.getComboPosition(element.type, true);
               element.y = this.getComboPosition(element.type, false);
               break;
            case CARD:
               element.x = this.getCardPosition(element.type, true);
               element.y = this.getCardPosition(element.type, false);
         }
      }
   }

   private void recordMoveOperation(ModConfigScreen.ElementType elementType, int oldX, int oldY, int newX, int newY) {
      if (oldX != newX || oldY != newY) {
         ModConfigScreen.MoveOperation operation = new ModConfigScreen.MoveOperation(this.currentEditMode, elementType, oldX, oldY, newX, newY);
         this.undoHistory.add(operation);
         if (this.undoHistory.size() > 10) {
            this.undoHistory.remove(0);
         }

         this.updateUndoButtonState();
      }
   }

   private void renderCustomButtons(DrawContext guiGraphics, int mouseX, int mouseY) {
      for (ModConfigScreen.CustomButton button : this.customMenuButtons) {
         button.render(guiGraphics, mouseX, mouseY);
      }
   }

   private void renderDraggableElements(DrawContext guiGraphics, int mouseX, int mouseY) {
      for (ModConfigScreen.DraggableElement element : this.draggableElements) {
         int drawX = this.width / 2 + element.x - element.width / 2;
         int drawY = this.height + element.y - element.height / 2;
         int borderColor = this.currentEditMode.color;
         int fillColor = borderColor & 16777215 | -2147483648;
         boolean isVisible = this.getElementVisible(element.type);
         if (!isVisible) {
            fillColor = borderColor & 16777215 | 1073741824;
         }

         guiGraphics.fill(drawX, drawY, drawX + element.width, drawY + element.height, fillColor);
         RenderHelper.drawBorder(guiGraphics, drawX, drawY, element.width, element.height, borderColor);
         String nameText = element.name;
         String coordText = "(" + element.x + ", " + element.y + ")";
         int nameWidth = this.textRenderer.getWidth(nameText);
         int coordWidth = this.textRenderer.getWidth(coordText);
         int totalTextHeight = 9 * 2 + 5;
         int textStartY = drawY + (element.height - totalTextHeight) / 2;
         guiGraphics.drawText(this.textRenderer, nameText, drawX + element.width / 2 - nameWidth / 2, textStartY, -1, true);
         guiGraphics.drawText(this.textRenderer, coordText, drawX + element.width / 2 - coordWidth / 2, textStartY + 9 + 5, -1, true);
         this.drawEyeIcon(guiGraphics, drawX, drawY, element.width, isVisible);
         if (element.contains(mouseX, mouseY, this.width, this.height)) {
            RenderHelper.drawBorder(guiGraphics, drawX - 1, drawY - 1, element.width + 2, element.height + 2, -1);
         }

         if (element == this.selectedElement) {
            RenderHelper.drawBorder(guiGraphics, drawX - 2, drawY - 2, element.width + 4, element.height + 4, -256);
         }

         if (this.isElementOutOfBounds(element, this.width, this.height)) {
            String warningText = "!";
            int warningWidth = this.textRenderer.getWidth(warningText);
            guiGraphics.drawText(this.textRenderer, warningText, drawX + element.width - warningWidth - 2, drawY + 2, -65536, true);
         }
      }
   }

   private void savePositionsToConfig() {
      for (ModConfigScreen.DraggableElement element : this.draggableElements) {
         switch (this.currentEditMode) {
            case SCROLL:
               this.setScrollPosition(element.type, element.x, element.y);
               break;
            case COMBO:
               this.setComboPosition(element.type, element.x, element.y);
               break;
            case CARD:
               this.setCardPosition(element.type, element.x, element.y);
         }
      }

      Config.saveConfig();
      Config.loadConfig();
   }

   private void switchEditMode(ModConfigScreen.EditMode newMode) {
      if (this.currentEditMode != newMode) {
         this.savePositionsToConfig();
         this.currentEditMode = newMode;
         this.loadPositionsFromConfig();
         this.clearUndoHistory();
         Config config = Config.getInstance();
         switch (newMode) {
            case SCROLL:
               config.ICON_MODE = Config.IconMode.SCROLLING;
               Config.iconMode = Config.IconMode.SCROLLING;
               break;
            case COMBO:
               config.ICON_MODE = Config.IconMode.COMBO;
               Config.iconMode = Config.IconMode.COMBO;
               break;
            case CARD:
               config.ICON_MODE = Config.IconMode.CARD;
               Config.iconMode = Config.IconMode.CARD;
         }

         Config.saveConfig();
         Config.loadConfig();
      }

      boolean isCardMode = newMode == ModConfigScreen.EditMode.CARD;

      for (ModConfigScreen.DraggableElement element : this.draggableElements) {
         if (element.type != ModConfigScreen.ElementType.ICON) {
            element.visibleInGame = !isCardMode;
         }
      }

      this.updateElementSizes();
      this.updateModeButtons();
   }

   private void toggleMenu() {
      this.isMenuExpanded = !this.isMenuExpanded;
      if (this.isMenuExpanded) {
         this.menuButton.setMessage(Text.literal("<"));
      } else {
         this.menuButton.setMessage(Text.literal("≡"));
      }
   }

   private void undoLastMove() {
      if (!this.undoHistory.isEmpty()) {
         ModConfigScreen.MoveOperation lastOperation = this.undoHistory.remove(this.undoHistory.size() - 1);

         for (ModConfigScreen.DraggableElement element : this.draggableElements) {
            if (element.type == lastOperation.elementType) {
               element.x = lastOperation.oldX;
               element.y = lastOperation.oldY;
               break;
            }
         }

         this.savePositionsToConfig();
         this.updateUndoButtonState();
      }
   }

   void updateElementSizes() {
      int iconBaseWidth = 70;
      int iconBaseHeight = 70;
      int subtitleBaseWidth = 200;
      int subtitleBaseHeight = 40;
      int scoreBaseWidth = 60;
      int scoreBaseHeight = 40;
      int bonusBaseWidth = 60;
      int bonusBaseHeight = 50;
      int iconConfigSize = this.getElementSize(ModConfigScreen.ElementType.ICON);
      int subtitleConfigSize = this.getElementSize(ModConfigScreen.ElementType.SUBTITLE);
      int scoreConfigSize = this.getElementSize(ModConfigScreen.ElementType.SCORE);
      int bonusConfigSize = this.getElementSize(ModConfigScreen.ElementType.BONUS);
      float iconScale = iconConfigSize / 32.0F;
      float subtitleScale = subtitleConfigSize / 100.0F;
      float scoreScale = scoreConfigSize / 250.0F;
      float bonusScale = bonusConfigSize / 100.0F;

      for (ModConfigScreen.DraggableElement element : this.draggableElements) {
         switch (element.type) {
            case BONUS:
               element.width = (int)(bonusBaseWidth * bonusScale);
               element.height = (int)(bonusBaseHeight * bonusScale);
            case BOTTOMBAR:
            default:
               break;
            case ICON:
               element.width = (int)(iconBaseWidth * iconScale);
               element.height = (int)(iconBaseHeight * iconScale);
               break;
            case SCORE:
               element.width = (int)(scoreBaseWidth * scoreScale);
               element.height = (int)(scoreBaseHeight * scoreScale);
               break;
            case SUBTITLE:
               element.width = (int)(subtitleBaseWidth * subtitleScale);
               element.height = (int)(subtitleBaseHeight * subtitleScale);
         }
      }
   }

   private void updateModeButtons() {
      for (int i = 0; i < this.modeButtons.length; i++) {
         ButtonWidget button = this.modeButtons[i];
         ModConfigScreen.EditMode buttonMode = this.getEditModeForIndex(i);
         if (buttonMode == this.currentEditMode) {
            button.setMessage(Text.literal(buttonMode.displayName).styled(style -> style.withColor(-256)));
         } else {
            button.setMessage(Text.literal(buttonMode.displayName).styled(style -> style.withColor(-1)));
         }
      }
   }

   private void updateScoreText() {
      Text.literal("您在此服务器上的分数: ")
         .copy()
         .append(Text.literal(String.valueOf(this.currentScore)).styled(style -> style.withColor(-65536).withBold(true)));
   }

   private void updateUndoButtonState() {
      this.canUndo = !this.undoHistory.isEmpty();
      if (this.undoButton != null) {
         this.undoButton.active = this.canUndo;
         if (this.canUndo) {
            this.undoButton.setMessage(Text.translatable("gd656killicon.modconfig.undo").styled(style -> style.withColor(-1)));
         } else {
            this.undoButton.setMessage(Text.translatable("gd656killicon.modconfig.undo").styled(style -> style.withColor(-7829368)));
         }
      }
   }

   private void requestScoreUpdate() {
      if (this.client != null && this.client.player != null) {
         ClientPlayNetworking.send(ScoreRequestPacket.INSTANCE);
         this.lastScoreUpdateTime = System.currentTimeMillis();
      }
   }

   private int getScrollPosition(ModConfigScreen.ElementType type, boolean isX) {
      return switch (type) {
         case BONUS -> isX ? Config.scrollBonusX : Config.scrollBonusY;
         case BOTTOMBAR -> 0;
         case ICON -> isX ? Config.scrollIconX : Config.scrollIconY;
         case SCORE -> isX ? Config.scrollScoreX : Config.scrollScoreY;
         case SUBTITLE -> isX ? Config.scrollSubtitleX : Config.scrollSubtitleY;
      };
   }

   private int getComboPosition(ModConfigScreen.ElementType type, boolean isX) {
      return switch (type) {
         case BONUS -> isX ? Config.comboBonusX : Config.comboBonusY;
         case BOTTOMBAR -> 0;
         case ICON -> isX ? Config.comboIconX : Config.comboIconY;
         case SCORE -> isX ? Config.comboScoreX : Config.comboScoreY;
         case SUBTITLE -> isX ? Config.comboSubtitleX : Config.comboSubtitleY;
      };
   }

   private int getCardPosition(ModConfigScreen.ElementType type, boolean isX) {
      return switch (type) {
         case BONUS -> isX ? Config.cardBonusX : Config.cardBonusY;
         case BOTTOMBAR -> 0;
         case ICON -> isX ? Config.cardIconX : Config.cardIconY;
         case SCORE -> isX ? Config.cardScoreX : Config.cardScoreY;
         case SUBTITLE -> isX ? Config.cardSubtitleX : Config.cardSubtitleY;
      };
   }

   private void setScrollPosition(ModConfigScreen.ElementType type, int x, int y) {
      Config config = Config.getInstance();
      switch (type) {
         case BONUS:
            config.SCROLL_BONUS_X = x;
            config.SCROLL_BONUS_Y = y;
            Config.scrollBonusX = x;
            Config.scrollBonusY = y;
         case BOTTOMBAR:
         default:
            break;
         case ICON:
            config.SCROLL_ICON_X = x;
            config.SCROLL_ICON_Y = y;
            Config.scrollIconX = x;
            Config.scrollIconY = y;
            break;
         case SCORE:
            config.SCROLL_SCORE_X = x;
            config.SCROLL_SCORE_Y = y;
            Config.scrollScoreX = x;
            Config.scrollScoreY = y;
            break;
         case SUBTITLE:
            config.SCROLL_SUBTITLE_X = x;
            config.SCROLL_SUBTITLE_Y = y;
            Config.scrollSubtitleX = x;
            Config.scrollSubtitleY = y;
      }

      Config.saveConfig();
   }

   private void setComboPosition(ModConfigScreen.ElementType type, int x, int y) {
      Config config = Config.getInstance();
      switch (type) {
         case BONUS:
            config.COMBO_BONUS_X = x;
            config.COMBO_BONUS_Y = y;
            Config.comboBonusX = x;
            Config.comboBonusY = y;
         case BOTTOMBAR:
         default:
            break;
         case ICON:
            config.COMBO_ICON_X = x;
            config.COMBO_ICON_Y = y;
            Config.comboIconX = x;
            Config.comboIconY = y;
            break;
         case SCORE:
            config.COMBO_SCORE_X = x;
            config.COMBO_SCORE_Y = y;
            Config.comboScoreX = x;
            Config.comboScoreY = y;
            break;
         case SUBTITLE:
            config.COMBO_SUBTITLE_X = x;
            config.COMBO_SUBTITLE_Y = y;
            Config.comboSubtitleX = x;
            Config.comboSubtitleY = y;
      }

      Config.saveConfig();
   }

   private void setCardPosition(ModConfigScreen.ElementType type, int x, int y) {
      Config config = Config.getInstance();
      switch (type) {
         case BONUS:
            config.CARD_BONUS_X = x;
            config.CARD_BONUS_Y = y;
            Config.cardBonusX = x;
            Config.cardBonusY = y;
         case BOTTOMBAR:
         default:
            break;
         case ICON:
            config.CARD_ICON_X = x;
            config.CARD_ICON_Y = y;
            Config.cardIconX = x;
            Config.cardIconY = y;
            break;
         case SCORE:
            config.CARD_SCORE_X = x;
            config.CARD_SCORE_Y = y;
            Config.cardScoreX = x;
            Config.cardScoreY = y;
            break;
         case SUBTITLE:
            config.CARD_SUBTITLE_X = x;
            config.CARD_SUBTITLE_Y = y;
            Config.cardSubtitleX = x;
            Config.cardSubtitleY = y;
      }

      Config.saveConfig();
   }

   private void drawEyeIcon(DrawContext guiGraphics, int drawX, int drawY, int elementWidth, boolean isVisible) {
      Identifier eyeIcon;
      if (isVisible) {
         eyeIcon = Identifier.of("minecraft", "textures/mob_effect/conduit_power.png");
      } else {
         eyeIcon = Identifier.of("minecraft", "textures/mob_effect/blindness.png");
      }

      try {
         int iconSize = 16;
         int iconX = drawX + elementWidth - iconSize - 3;
         int iconY = drawY + 3;
         guiGraphics.drawTexture(
            net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED,
            eyeIcon,
            iconX,
            iconY,
            0.0F,
            0.0F,
            iconSize,
            iconSize,
            iconSize,
            iconSize,
            iconSize,
            iconSize,
            -1
         );
      } catch (Exception var13) {
         String eyeText = isVisible ? "○" : "×";
         int textWidth = this.textRenderer.getWidth(eyeText);
         int textX = drawX + elementWidth - textWidth - 3;
         int textY = drawY + 3;
         int textColor = isVisible ? -16711936 : -65536;
         guiGraphics.drawText(this.textRenderer, eyeText, textX, textY, textColor, true);
      }
   }

   private boolean isElementOutOfBounds(ModConfigScreen.DraggableElement element, int screenWidth, int screenHeight) {
      int drawX = screenWidth / 2 + element.x - element.width / 2;
      int drawY = screenHeight + element.y - element.height / 2;
      return drawX < 0 || drawX + element.width > screenWidth || drawY < 0 || drawY + element.height > screenHeight;
   }

   public static class CustomButton {
      public final int height;
      public final int width;
      public final int x;
      public final int y;
      public final Runnable onClick;
      public final String label;
      public boolean isHovered = false;

      public CustomButton(String label, int x, int y, int width, int height, Runnable onClick) {
         this.label = label;
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height;
         this.onClick = onClick;
      }

      public boolean contains(int mouseX, int mouseY) {
         return mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.height;
      }

      public void render(DrawContext guiGraphics, int mouseX, int mouseY) {
         this.isHovered = this.contains(mouseX, mouseY);
         int fillColor = this.isHovered ? -12566464 : -16777216;
         int borderColor = -8355712;
         guiGraphics.fill(this.x, this.y, this.x + this.width, this.y + this.height, fillColor);
         RenderHelper.drawBorder(guiGraphics, this.x, this.y, this.width, this.height, borderColor);
         MinecraftClient mc = MinecraftClient.getInstance();
         int textWidth = mc.textRenderer.getWidth(this.label);
         int textX = this.x + (this.width - textWidth) / 2;
         int textY = this.y + (this.height - 9) / 2 + 1;
         guiGraphics.drawText(mc.textRenderer, this.label, textX, textY, -1, true);
      }
   }

   public static class DraggableElement {
      public final int defaultX;
      public final int defaultY;
      public final ModConfigScreen.ElementType type;
      public final String name;
      public int dragOffsetX;
      public int dragOffsetY;
      public int height;
      public int width;
      public int x;
      public int y;
      public boolean visibleInGame;
      private Object userData;

      public DraggableElement(String name, ModConfigScreen.ElementType type, int defaultX, int defaultY, int width, int height, boolean visibleInGame) {
         this.name = name;
         this.type = type;
         this.x = defaultX;
         this.y = defaultY;
         this.defaultX = defaultX;
         this.defaultY = defaultY;
         this.width = width;
         this.height = height;
         this.visibleInGame = visibleInGame;
      }

      public boolean contains(int mouseX, int mouseY, int screenWidth, int screenHeight) {
         int drawX = screenWidth / 2 + this.x - this.width / 2;
         int drawY = screenHeight + this.y - this.height / 2;
         return mouseX >= drawX && mouseX <= drawX + this.width && mouseY >= drawY && mouseY <= drawY + this.height;
      }

      public void setDragOffset(int mouseX, int mouseY, int screenWidth, int screenHeight) {
         int drawX = screenWidth / 2 + this.x - this.width / 2;
         int drawY = screenHeight + this.y - this.height / 2;
         this.dragOffsetX = mouseX - drawX;
         this.dragOffsetY = mouseY - drawY;
      }

      public void setPositionFromMouse(int mouseX, int mouseY, int screenWidth, int screenHeight) {
         int newX = mouseX - screenWidth / 2 - this.dragOffsetX + this.width / 2;
         int newY = mouseY - screenHeight - this.dragOffsetY + this.height / 2;
         this.x = this.clampToScreenBounds(newX, screenWidth, true);
         this.y = this.clampToScreenBounds(newY, screenHeight, false);
      }

      private int clampToScreenBounds(int coordinate, int screenSize, boolean isX) {
         int margin = 20;
         if (isX) {
            int maxX = screenSize / 2 - this.width / 2 - margin;
            int minX = -screenSize / 2 + this.width / 2 + margin;
            return MathHelper.clamp(coordinate, minX, maxX);
         } else {
            int maxY = -this.height / 2 - margin;
            int minY = -screenSize + this.height / 2 + margin;
            return MathHelper.clamp(coordinate, minY, maxY);
         }
      }

      public Object getUserData() {
         return this.userData;
      }

      public void setUserData(Object userData) {
         this.userData = userData;
      }
   }

   private static enum EditMode {
      SCROLL("滚动模式", -65536),
      COMBO("连杀模式", -16711936),
      CARD("卡牌模式", -256);

      public final String displayName;
      public final int color;

      private EditMode(String displayName, int color) {
         this.displayName = displayName;
         this.color = color;
      }
   }

   public static enum ElementType {
      BONUS,
      BOTTOMBAR,
      ICON,
      SCORE,
      SUBTITLE;
   }

   public static class MoveOperation {
      public final ModConfigScreen.EditMode editMode;
      public final ModConfigScreen.ElementType elementType;
      public final int newX;
      public final int newY;
      public final int oldX;
      public final int oldY;
      public final long timestamp;

      public MoveOperation(ModConfigScreen.EditMode editMode, ModConfigScreen.ElementType elementType, int oldX, int oldY, int newX, int newY) {
         this.editMode = editMode;
         this.elementType = elementType;
         this.oldX = oldX;
         this.oldY = oldY;
         this.newX = newX;
         this.newY = newY;
         this.timestamp = System.currentTimeMillis();
      }
   }
}
