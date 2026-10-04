package org.mods.gd656killicon.client;

import com.mojang.logging.LogUtils;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.MouseInput;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.mods.gd656killicon.Config;

public class ConfigScreen extends Screen {
   private final Screen parent;
   private ConfigScreen.OptionsList list;
   private static ConfigScreen instance;
   private ButtonWidget resetButton;
   private boolean resetConfirmation = false;
   private long resetConfirmationTime = 0L;
   private static final int BACK_BUTTON_WIDTH = 80;
   private static final int BACK_BUTTON_HEIGHT = 20;
   private static final int BACK_PADDING = 10;
   private static final int BUTTON_WIDTH = 200;
   private static final int BUTTON_HEIGHT = 20;
   private static final int OPTIONS_LIST_TOP_HEIGHT = 26;
   private static final int OPTIONS_LIST_BOTTOM_OFFSET = 32;
   private static final int OPTIONS_LIST_ITEM_HEIGHT = 25;
   private static final int TITLE_HEIGHT = 10;
   private static final long RESET_CONFIRMATION_TIMEOUT = 3000L;

   public ConfigScreen(Screen parent) {
      super(Text.translatable("gd656killicon.config.title"));
      this.parent = parent;
   }

   public static ConfigScreen getInstance() {
      return instance;
   }

   public void close() {
      instance = null;
      if (this.client != null) {
         this.client.setScreen(this.parent);
      }
   }

   protected void init() {
      super.init();
      int listTop = 26;
      int listBottom = this.height - 32;
      int listHeight = listBottom - listTop;
      if (this.list == null) {
         this.list = new ConfigScreen.OptionsList(this.client, this.width, listHeight, listTop, 25);
      } else {
         this.list.position(this.width, listHeight, 0, listTop);
      }

      this.clearChildren();
      this.addSelectableChild(this.list);
      instance = this;
      this.addDrawableChild(ButtonWidget.builder(Text.translatable("gd656killicon.modconfig.back"), button -> {
         if (this.client != null) {
            this.client.setScreen(this.parent);
         }
      }).dimensions(this.width - 80 - 10, 10, 80, 20).build());
      int totalButtonsWidth = 410;
      int startX = (this.width - totalButtonsWidth) / 2;
      int buttonY = this.height - 20 - 6;
      this.resetButton = ButtonWidget.builder(Text.translatable("gd656killicon.config.reset"), button -> {
         if (this.resetConfirmation) {
            this.resetToDefaults();
            this.resetConfirmation = false;
            this.resetButton.setMessage(Text.translatable("gd656killicon.config.reset"));
         } else {
            this.resetConfirmation = true;
            this.resetConfirmationTime = System.currentTimeMillis();
            this.resetButton.setMessage(Text.translatable("gd656killicon.config.reset_confirm"));
         }
      }).dimensions(startX, buttonY, 200, 20).build();
      this.addDrawableChild(this.resetButton);
      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> {
         this.saveConfig();
         if (this.client != null) {
            this.client.setScreen(this.parent);
         }
      }).dimensions(startX + 200 + 10, buttonY, 200, 20).build());
   }

   public void render(@NotNull DrawContext guiGraphics, int mouseX, int mouseY, float partialTick) {
      if (this.list != null) {
         try {
            this.list.render(guiGraphics, mouseX, mouseY, partialTick);
         } catch (Exception var6) {
            LogUtils.getLogger().error("Error rendering options list: {}", var6.getMessage());
         }
      }

      guiGraphics.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, -1);
      if (this.resetConfirmation && System.currentTimeMillis() - this.resetConfirmationTime > 3000L) {
         this.resetConfirmation = false;
         if (this.resetButton != null) {
            this.resetButton.setMessage(Text.translatable("gd656killicon.config.reset"));
         }
      }

      super.render(guiGraphics, mouseX, mouseY, partialTick);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      double adjustedDelta = verticalAmount * Config.SCROLL_SENSITIVITY;
      return this.list != null && this.list.isMouseOver(mouseX, mouseY)
         ? this.list.mouseScrolled(mouseX, mouseY, 0.0, adjustedDelta)
         : super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
   }

   public boolean mouseClicked(Click click, boolean doubled) {
      double mouseX = click.x();
      double mouseY = click.y();
      boolean clickedEditBox = false;
      if (this.list != null && this.list.isMouseOver(mouseX, mouseY)) {
         for (ConfigScreen.OptionsList.Entry entry : this.list.children()) {
            if (entry instanceof ConfigScreen.OptionsList.EditBoxOptionEntry editBoxEntry && editBoxEntry.editBox.isMouseOver(mouseX, mouseY)) {
               this.setFocused(editBoxEntry.editBox);
               clickedEditBox = editBoxEntry.editBox.mouseClicked(click, doubled);
               break;
            }
         }
      }

      if (clickedEditBox) {
         return true;
      } else {
         this.setFocused(null);
         return this.list != null && this.list.isMouseOver(mouseX, mouseY)
            ? this.list.mouseClicked(click, doubled)
            : super.mouseClicked(click, doubled);
      }
   }

   public boolean mouseReleased(Click click) {
      return this.list != null && this.list.isMouseOver(click.x(), click.y())
         ? this.list.mouseReleased(click)
         : super.mouseReleased(click);
   }

   public boolean mouseDragged(Click click, double dragX, double dragY) {
      return this.list != null && this.list.isMouseOver(click.x(), click.y())
         ? this.list.mouseDragged(click, dragX, dragY)
         : super.mouseDragged(click, dragX, dragY);
   }

   private void saveConfig() {
      Config config = Config.getInstance();
      config.KILL_BONUS_DISPLAY = this.list.killBonusDisplay.getValue();
      config.ASSIST_BONUS_DISPLAY = this.list.assistBonusDisplay.getValue();
      config.CRITICAL_BONUS_DISPLAY = this.list.criticalBonusDisplay.getValue();
      config.LONGRANGE_BONUS_DISPLAY = this.list.longrangeBonusDisplay.getValue();
      config.MAGIC_BONUS_DISPLAY = this.list.magicBonusDisplay.getValue();
      config.HAND_BONUS_DISPLAY = this.list.handBonusDisplay.getValue();
      config.COMBO_BONUS_DISPLAY = this.list.comboBonusDisplay.getValue();
      config.DAMAGE_BONUS_DISPLAY = this.list.damageBonusDisplay.getValue();
      config.KILL_ICON_DURATION = this.list.killIconDuration.getValue();
      config.KILL_ICON_SIZE = this.list.killIconSize.getValue();
      Config.WEAPON_COLOR = this.list.weaponColor.getValue();
      Config.TARGET_COLOR = this.list.targetColor.getValue();
      config.CUSTOM_SUBTITLE_FORMAT = this.list.customSubtitleFormat.getValue();
      config.SOUND_VOLUME = this.list.soundVolume.getValue();
      Config.CRITICAL_COLOR = this.list.criticalColor.getValue();
      config.ICON_MODE = this.list.iconMode.getValue();
      config.COMBO_SCORE_FONT_SCALE = this.list.comboScoreFontScale.getValue();
      Config.FLASHING_COLOR = this.list.flashingColor.getValue();
      Config.HIGH_SCORE_COLOR = this.list.highScoreColor.getValue();
      config.COMBO_TIMEOUT = this.list.comboTimeout.getValue();
      config.SCROLL_ANIMATION_SPEED = this.list.scrollAnimationSpeed.getValue() / 100.0;
      config.ASSIST_SUBTITLE_FORMAT = this.list.assistSubtitleFormat.getValue();
      Config.SCROLL_SENSITIVITY = this.list.scrollSensitivity.getValue() / 100.0;
      config.FORCE_HIDE_COUNT = this.list.forceHideCount.getValue();
      config.MAX_DISPLAY_COUNT = this.list.maxDisplayCount.getValue();
      config.ICON_STYLE = this.list.iconStyle.getValue();
      config.CARD_ICON_STYLE = this.list.cardIconStyle.getValue();
      config.ENABLE_SOUND_EFFECTS = this.list.enableSoundEffects.getValue();
      Config.saveConfig();
      Config.loadConfig();
      this.resetConfirmation = false;
      this.resetButton.setMessage(Text.translatable("gd656killicon.config.reset"));
   }

   private void resetToDefaults() {
      Config.resetAllToDefaults();
      if (this.list != null) {
         this.list.killBonusDisplay.setValue("击败生物 +{score}");
         this.list.assistBonusDisplay.setValue("助攻击败 +{score}");
         this.list.criticalBonusDisplay.setValue("暴击加成 +{score}");
         this.list.magicBonusDisplay.setValue("魔法伤害 +{score}");
         this.list.handBonusDisplay.setValue("空手攻击 +{score}");
         this.list.damageBonusDisplay.setValue("造成伤害 +{score}");
         this.list.longrangeBonusDisplay.setValue("远距离击败 [6]{distance}[f] +{score}");
         this.list.comboBonusDisplay.setValue("[6]{combo}[f] 连杀 ! +{score}");
         this.list.killIconDuration.setValue(60);
         this.list.killIconSize.setValue(32);
         this.list.weaponColor.setValue("008080");
         this.list.targetColor.setValue("008080");
         this.list.customSubtitleFormat.setValue("你 使用 {weapon} 击败了 {target}");
         this.list.assistSubtitleFormat.setValue("你 助攻击败了 {target}");
         this.list.soundVolume.setValue(100);
         this.list.criticalColor.setValue("FFD700");
         this.list.iconMode.setValue(Config.IconMode.SCROLLING);
         this.list.comboScoreFontScale.setValue(250);
         this.list.flashingColor.setValue("DCDCDC");
         this.list.highScoreColor.setValue("FFD700");
         this.list.comboTimeout.setValue(3);
         this.list.scrollAnimationSpeed.setValue(20);
         this.list.scrollSensitivity.setValue(100);
         this.list.iconStyle.setValue(Config.IconStyle.MODERN);
         this.list.cardIconStyle.setValue(Config.IconStyle.MODERN);
         this.list.forceHideCount.setValue(9);
         this.list.maxDisplayCount.setValue(30);
         this.list.enableSoundEffects.setValue(true);
      }
   }

   class OptionsList extends ElementListWidget<ConfigScreen.OptionsList.Entry> {
      public ConfigScreen.OptionsList.EditBoxOptionEntry killBonusDisplay;
      public ConfigScreen.OptionsList.EditBoxOptionEntry assistBonusDisplay;
      public ConfigScreen.OptionsList.EditBoxOptionEntry criticalBonusDisplay;
      public ConfigScreen.OptionsList.EditBoxOptionEntry longrangeBonusDisplay;
      public ConfigScreen.OptionsList.EditBoxOptionEntry magicBonusDisplay;
      public ConfigScreen.OptionsList.EditBoxOptionEntry handBonusDisplay;
      public ConfigScreen.OptionsList.EditBoxOptionEntry comboBonusDisplay;
      public ConfigScreen.OptionsList.EditBoxOptionEntry damageBonusDisplay;
      public ConfigScreen.OptionsList.SliderOptionEntry killIconDuration;
      public ConfigScreen.OptionsList.SliderOptionEntry killIconSize;
      public ConfigScreen.OptionsList.EditBoxOptionEntry customSubtitleFormat;
      public ConfigScreen.OptionsList.SliderOptionEntry soundVolume;
      public ConfigScreen.OptionsList.EditBoxOptionEntry weaponColor;
      public ConfigScreen.OptionsList.EditBoxOptionEntry targetColor;
      public ConfigScreen.OptionsList.EditBoxOptionEntry criticalColor;
      public ConfigScreen.OptionsList.ModeOptionEntry iconMode;
      public ConfigScreen.OptionsList.SliderOptionEntry comboScoreFontScale;
      public ConfigScreen.OptionsList.EditBoxOptionEntry flashingColor;
      public ConfigScreen.OptionsList.EditBoxOptionEntry highScoreColor;
      public ConfigScreen.OptionsList.SliderOptionEntry comboTimeout;
      public ConfigScreen.OptionsList.SliderOptionEntry scrollAnimationSpeed;
      public ConfigScreen.OptionsList.EditBoxOptionEntry assistSubtitleFormat;
      public ConfigScreen.OptionsList.SliderOptionEntry scrollSensitivity;
      public ConfigScreen.OptionsList.IconStyleOptionEntry iconStyle;
      public ConfigScreen.OptionsList.SliderOptionEntry forceHideCount;
      public ConfigScreen.OptionsList.SliderOptionEntry maxDisplayCount;
      public ConfigScreen.OptionsList.IconStyleOptionEntry cardIconStyle;
      public ConfigScreen.OptionsList.BooleanOptionEntry enableSoundEffects;

      public OptionsList(MinecraftClient minecraft, int width, int height, int y0, int itemHeight) {
         super(minecraft, width, height, y0, itemHeight);
         this.addEntry(new ConfigScreen.OptionsList.SpacerEntry());
         this.addEntry(new ConfigScreen.OptionsList.CategoryEntry(Text.translatable("gd656killicon.config.category.sound")));
         this.enableSoundEffects = new ConfigScreen.OptionsList.BooleanOptionEntry(
            Text.translatable("gd656killicon.config.enableSoundEffects"), Config.enableSoundEffects
         );
         this.addEntry(this.enableSoundEffects);
         this.soundVolume = new ConfigScreen.OptionsList.SliderOptionEntry(Text.translatable("gd656killicon.config.soundVolume"), 0, 125, Config.soundVolume);
         this.addEntry(this.soundVolume);
         this.addEntry(new ConfigScreen.OptionsList.CategoryEntry(Text.translatable("gd656killicon.config.category.bonus_display")));
         this.killBonusDisplay = new ConfigScreen.OptionsList.EditBoxOptionEntry(
            Text.translatable("gd656killicon.config.killBonusDisplay"), Config.killBonusDisplay, 200
         );
         this.addEntry(this.killBonusDisplay);
         this.assistBonusDisplay = new ConfigScreen.OptionsList.EditBoxOptionEntry(
            Text.translatable("gd656killicon.config.assistBonusDisplay"), Config.assistBonusDisplay, 200
         );
         this.addEntry(this.assistBonusDisplay);
         this.criticalBonusDisplay = new ConfigScreen.OptionsList.EditBoxOptionEntry(
            Text.translatable("gd656killicon.config.criticalBonusDisplay"), Config.criticalBonusDisplay, 200
         );
         this.addEntry(this.criticalBonusDisplay);
         this.magicBonusDisplay = new ConfigScreen.OptionsList.EditBoxOptionEntry(
            Text.translatable("gd656killicon.config.magicBonusDisplay"), Config.magicBonusDisplay, 200
         );
         this.addEntry(this.magicBonusDisplay);
         this.handBonusDisplay = new ConfigScreen.OptionsList.EditBoxOptionEntry(
            Text.translatable("gd656killicon.config.handBonusDisplay"), Config.handBonusDisplay, 200
         );
         this.addEntry(this.handBonusDisplay);
         this.comboBonusDisplay = new ConfigScreen.OptionsList.EditBoxOptionEntry(
            Text.translatable("gd656killicon.config.comboBonusDisplay"), Config.comboBonusDisplay, 200
         );
         this.addEntry(this.comboBonusDisplay);
         this.damageBonusDisplay = new ConfigScreen.OptionsList.EditBoxOptionEntry(
            Text.translatable("gd656killicon.config.damageBonusDisplay"), Config.damageBonusDisplay, 200
         );
         this.addEntry(this.damageBonusDisplay);
         this.longrangeBonusDisplay = new ConfigScreen.OptionsList.EditBoxOptionEntry(
            Text.translatable("gd656killicon.config.longrangeBonusDisplay"), Config.longrangeBonusDisplay, 200
         );
         this.addEntry(this.longrangeBonusDisplay);
         this.addEntry(new ConfigScreen.OptionsList.CategoryEntry(Text.translatable("gd656killicon.config.category.combo_score")));
         this.comboScoreFontScale = new ConfigScreen.OptionsList.SliderOptionEntry(
            Text.translatable("gd656killicon.config.comboScoreFontScale"), 100, 500, Config.comboScoreFontScale
         );
         this.addEntry(this.comboScoreFontScale);
         this.comboTimeout = new ConfigScreen.OptionsList.SliderOptionEntry(Text.translatable("gd656killicon.config.comboTimeout"), 1, 10, Config.comboTimeout);
         this.addEntry(this.comboTimeout);
         this.scrollAnimationSpeed = new ConfigScreen.OptionsList.SliderOptionEntry(
            Text.translatable("gd656killicon.config.scrollAnimationSpeed"), 10, 100, (int)(Config.scrollAnimationSpeed * 100.0)
         );
         this.addEntry(this.scrollAnimationSpeed);
         this.addEntry(new ConfigScreen.OptionsList.SpacerEntry());
         this.addEntry(new ConfigScreen.OptionsList.CategoryEntry(Text.translatable("gd656killicon.config.category.kill_icon")));
         this.killIconDuration = new ConfigScreen.OptionsList.SliderOptionEntry(
            Text.translatable("gd656killicon.config.killIconDuration"), 20, 200, Config.killIconDuration
         );
         this.addEntry(this.killIconDuration);
         this.killIconSize = new ConfigScreen.OptionsList.SliderOptionEntry(Text.translatable("gd656killicon.config.killIconSize"), 16, 64, Config.killIconSize);
         this.addEntry(this.killIconSize);
         this.iconMode = new ConfigScreen.OptionsList.ModeOptionEntry(Text.translatable("gd656killicon.config.iconMode"), Config.iconMode);
         this.addEntry(this.iconMode);
         this.iconStyle = new ConfigScreen.OptionsList.IconStyleOptionEntry(Text.translatable("gd656killicon.config.iconStyle"), Config.iconStyle);
         this.addEntry(this.iconStyle);
         this.cardIconStyle = new ConfigScreen.OptionsList.IconStyleOptionEntry(Text.translatable("gd656killicon.config.cardIconStyle"), Config.cardIconStyle);
         this.addEntry(this.cardIconStyle);
         this.forceHideCount = new ConfigScreen.OptionsList.SliderOptionEntry(
            Text.translatable("gd656killicon.config.forceHideCount"), 3, 20, Config.forceHideCount
         );
         this.addEntry(this.forceHideCount);
         this.maxDisplayCount = new ConfigScreen.OptionsList.SliderOptionEntry(
            Text.translatable("gd656killicon.config.maxDisplayCount"), 5, 30, Config.maxDisplayCount
         );
         this.addEntry(this.maxDisplayCount);
         this.addEntry(new ConfigScreen.OptionsList.SpacerEntry());
         this.addEntry(new ConfigScreen.OptionsList.CategoryEntry(Text.translatable("gd656killicon.config.category.display_control")));
         this.scrollSensitivity = new ConfigScreen.OptionsList.SliderOptionEntry(
            Text.translatable("gd656killicon.config.scrollSensitivity"), 10, 200, (int)(Config.SCROLL_SENSITIVITY * 100.0)
         );
         this.addEntry(this.scrollSensitivity);
         this.customSubtitleFormat = new ConfigScreen.OptionsList.EditBoxOptionEntry(
            Text.translatable("gd656killicon.config.customSubtitleFormat"), Config.customSubtitleFormat, 200
         );
         this.addEntry(this.customSubtitleFormat);
         this.assistSubtitleFormat = new ConfigScreen.OptionsList.EditBoxOptionEntry(
            Text.translatable("gd656killicon.config.assistSubtitleFormat"), Config.assistSubtitleFormat, 200
         );
         this.addEntry(this.assistSubtitleFormat);
         this.addEntry(new ConfigScreen.OptionsList.SpacerEntry());
         this.addEntry(new ConfigScreen.OptionsList.CategoryEntry(Text.translatable("gd656killicon.config.category.text_colors")));
         this.weaponColor = new ConfigScreen.OptionsList.EditBoxOptionEntry(Text.translatable("gd656killicon.config.weaponColor"), Config.WEAPON_COLOR);
         this.addEntry(this.weaponColor);
         this.targetColor = new ConfigScreen.OptionsList.EditBoxOptionEntry(Text.translatable("gd656killicon.config.targetColor"), Config.TARGET_COLOR);
         this.addEntry(this.targetColor);
         this.criticalColor = new ConfigScreen.OptionsList.EditBoxOptionEntry(Text.translatable("gd656killicon.config.criticalColor"), Config.CRITICAL_COLOR);
         this.addEntry(this.criticalColor);
         this.highScoreColor = new ConfigScreen.OptionsList.EditBoxOptionEntry(
            Text.translatable("gd656killicon.config.highScoreColor"), Config.HIGH_SCORE_COLOR
         );
         this.addEntry(this.highScoreColor);
         this.flashingColor = new ConfigScreen.OptionsList.EditBoxOptionEntry(Text.translatable("gd656killicon.config.flashingColor"), Config.FLASHING_COLOR);
         this.addEntry(this.flashingColor);
         this.addEntry(new ConfigScreen.OptionsList.SpacerEntry());
      }

      public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
         if (this.getMaxScrollY() <= 0) {
            return false;
         } else {
            double scrollAmount = -verticalAmount * Config.SCROLL_SENSITIVITY * (double)this.itemHeight / 2.0;
            this.setScrollY(this.getScrollY() + scrollAmount);
            return true;
         }
      }

      class BooleanOptionEntry extends ConfigScreen.OptionsList.Entry {
         private final CheckboxWidget checkbox;
         private final Text label;

         public BooleanOptionEntry(Text label, boolean initialValue) {
            this.label = label;
            this.checkbox = CheckboxWidget.builder(Text.empty(), ConfigScreen.this.textRenderer).pos(0, 0).checked(initialValue).build();
         }

         public void setValue(boolean value) {
            if (this.checkbox.isChecked() != value) {
               this.checkbox.onPress(new Click(0.0, 0.0, new MouseInput(0, 0)));
            }
         }

         public void render(DrawContext guiGraphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
            int top = this.getY();
            int left = this.getX();
            int width = this.getWidth();
            guiGraphics.drawTextWithShadow(ConfigScreen.this.textRenderer, this.label, left, top + 5, -1);
            this.checkbox.setX(left + width - 150);
            this.checkbox.setY(top);
            this.checkbox.render(guiGraphics, mouseX, mouseY, partialTick);
         }

         public boolean getValue() {
            return this.checkbox.isChecked();
         }

         @NotNull
         public List<? extends Element> children() {
            return List.of(this.checkbox);
         }

         @NotNull
         public List<? extends Selectable> selectableChildren() {
            return List.of(this.checkbox);
         }
      }

      class CategoryEntry extends ConfigScreen.OptionsList.Entry {
         private final Text text;

         public CategoryEntry(Text text) {
            this.text = text;
         }

         public void render(DrawContext guiGraphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
            int top = this.getY();
            int left = this.getX();
            int width = this.getWidth();
            guiGraphics.drawCenteredTextWithShadow(ConfigScreen.this.textRenderer, this.text, left + width / 2, top + 5, -1);
         }

         @NotNull
         public List<? extends Element> children() {
            return List.of();
         }

         @NotNull
         public List<? extends Selectable> selectableChildren() {
            return List.of();
         }
      }

      class EditBoxOptionEntry extends ConfigScreen.OptionsList.Entry {
         private final TextFieldWidget editBox;
         private final Text label;
         private final boolean isColorOption;

         public EditBoxOptionEntry(Text label, String initialValue) {
            this(label, initialValue, 100, true);
         }

         public EditBoxOptionEntry(Text label, String initialValue, int width) {
            this(label, initialValue, width, false);
         }

         public EditBoxOptionEntry(Text label, String initialValue, int width, boolean isColorOption) {
            this.label = label;
            this.isColorOption = isColorOption;
            this.editBox = new TextFieldWidget(ConfigScreen.this.textRenderer, 0, 0, width, 20, Text.empty());
            this.editBox.setText(initialValue);
            this.editBox.setMaxLength(isColorOption ? 6 : 256);
            this.editBox.setFocused(true);
            this.editBox.setFocusUnlocked(true);
         }

         public void setValue(String value) {
            this.editBox.setText(value);
         }

         public void render(DrawContext guiGraphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
            int top = this.getY();
            int left = this.getX();
            int width = this.getWidth();
            guiGraphics.drawTextWithShadow(ConfigScreen.this.textRenderer, this.label, left, top + 5, -1);
            if (this.isColorOption) {
               this.editBox.setX(left + width - 150 + 75);
               this.editBox.setY(top);
               this.editBox.setWidth(75);
               this.editBox.render(guiGraphics, mouseX, mouseY, partialTick);
               int colorBoxX = left + width - 150 + 130;
               int colorBoxSize = 20;
               String colorText = this.editBox.getText();

               int colorValue;
               try {
                  String colorStr = "FF" + colorText.replace("#", "");
                  colorValue = (int)Long.parseLong(colorStr, 16);
               } catch (Exception var16) {
                  colorValue = -1;
               }

               guiGraphics.fill(colorBoxX, top, colorBoxX + colorBoxSize, top + colorBoxSize, colorValue);
               guiGraphics.fill(colorBoxX, top, colorBoxX + 1, top + colorBoxSize, -16777216);
               guiGraphics.fill(colorBoxX + colorBoxSize - 1, top, colorBoxX + colorBoxSize, top + colorBoxSize, -16777216);
               guiGraphics.fill(colorBoxX, top, colorBoxX + colorBoxSize, top + 1, -16777216);
               guiGraphics.fill(colorBoxX, top + colorBoxSize - 1, colorBoxX + colorBoxSize, top + colorBoxSize, -16777216);
            } else {
               this.editBox.setX(left + width - 150 + 50);
               this.editBox.setY(top);
               this.editBox.setWidth(100);
               this.editBox.render(guiGraphics, mouseX, mouseY, partialTick);
            }
         }

         public String getValue() {
            return this.editBox.getText();
         }

         @NotNull
         public List<? extends Element> children() {
            return List.of(this.editBox);
         }

         @NotNull
         public List<? extends Selectable> selectableChildren() {
            return List.of(this.editBox);
         }
      }

      abstract static class Entry extends ElementListWidget.Entry<ConfigScreen.OptionsList.Entry> {
      }

      class IconStyleOptionEntry extends ConfigScreen.OptionsList.Entry {
         private final CyclingButtonWidget<Config.IconStyle> styleButton;
         private final Text label;

         public IconStyleOptionEntry(Text label, Config.IconStyle initialValue) {
            this.label = label;
            this.styleButton = CyclingButtonWidget.<Config.IconStyle>builder(style -> Text.translatable(style.getTranslationKey()), initialValue)
               .values(Config.IconStyle.values())
               .build(0, 0, 150, 20, Text.empty());
         }

         public void setValue(Config.IconStyle value) {
            this.styleButton.setValue(value);
         }

         public void render(DrawContext guiGraphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
            int top = this.getY();
            int left = this.getX();
            int width = this.getWidth();
            guiGraphics.drawTextWithShadow(ConfigScreen.this.textRenderer, this.label, left, top + 5, -1);
            this.styleButton.setX(left + width - 150);
            this.styleButton.setY(top);
            this.styleButton.render(guiGraphics, mouseX, mouseY, partialTick);
         }

         public Config.IconStyle getValue() {
            return this.styleButton.getValue();
         }

         @NotNull
         public List<? extends Element> children() {
            return List.of(this.styleButton);
         }

         @NotNull
         public List<? extends Selectable> selectableChildren() {
            return List.of(this.styleButton);
         }
      }

      class ModeOptionEntry extends ConfigScreen.OptionsList.Entry {
         private final CyclingButtonWidget<Config.IconMode> modeButton;
         private final Text label;

         public ModeOptionEntry(Text label, Config.IconMode initialValue) {
            this.label = label;
            this.modeButton = CyclingButtonWidget.<Config.IconMode>builder(mode -> Text.translatable(mode.getTranslationKey()), initialValue)
               .values(Config.IconMode.values())
               .build(0, 0, 150, 20, Text.empty());
         }

         public void setValue(Config.IconMode value) {
            this.modeButton.setValue(value);
         }

         public void render(DrawContext guiGraphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
            int top = this.getY();
            int left = this.getX();
            int width = this.getWidth();
            guiGraphics.drawTextWithShadow(ConfigScreen.this.textRenderer, this.label, left, top + 5, -1);
            this.modeButton.setX(left + width - 150);
            this.modeButton.setY(top);
            this.modeButton.render(guiGraphics, mouseX, mouseY, partialTick);
         }

         public Config.IconMode getValue() {
            return this.modeButton.getValue();
         }

         @NotNull
         public List<? extends Element> children() {
            return List.of(this.modeButton);
         }

         @NotNull
         public List<? extends Selectable> selectableChildren() {
            return List.of(this.modeButton);
         }
      }

      class SliderOptionEntry extends ConfigScreen.OptionsList.Entry {
         private final ConfigScreen.OptionsList.SliderOptionEntry.CustomSlider slider;
         private final Text label;

         public SliderOptionEntry(Text label, int min, int max, int initialValue) {
            this.label = label;
            this.slider = new ConfigScreen.OptionsList.SliderOptionEntry.CustomSlider(
               0, 0, 150, 20, Text.empty(), (double)(initialValue - min) / (max - min), min, max
            );
         }

         public void setValue(int value) {
            this.slider.setIntValue(value);
         }

         public void render(DrawContext guiGraphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
            int top = this.getY();
            int left = this.getX();
            int width = this.getWidth();
            guiGraphics.drawTextWithShadow(ConfigScreen.this.textRenderer, this.label, left, top + 5, -1);
            this.slider.setX(left + width - 150);
            this.slider.setY(top);
            this.slider.render(guiGraphics, mouseX, mouseY, partialTick);
         }

         public int getValue() {
            return this.slider.getIntValue();
         }

         @NotNull
         public List<? extends Element> children() {
            return List.of(this.slider);
         }

         @NotNull
         public List<? extends Selectable> selectableChildren() {
            return List.of(this.slider);
         }

         static class CustomSlider extends SliderWidget {
            private final int min;
            private final int max;

            public CustomSlider(int x, int y, int width, int height, Text message, double value, int min, int max) {
               super(x, y, width, height, message, value);
               this.min = min;
               this.max = max;
               this.updateMessage();
            }

            protected void updateMessage() {
               this.setMessage(Text.literal(this.getIntValue() + "%"));
            }

            protected void applyValue() {
            }

            public int getIntValue() {
               return this.min + (int)(this.value * (this.max - this.min));
            }

            public void setIntValue(int value) {
               this.value = MathHelper.clamp((double)(value - this.min) / (this.max - this.min), 0.0, 1.0);
               this.updateMessage();
            }
         }
      }

      static class SpacerEntry extends ConfigScreen.OptionsList.Entry {
         public void render(DrawContext guiGraphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
         }

         @NotNull
         public List<? extends Element> children() {
            return List.of();
         }

         @NotNull
         public List<? extends Selectable> selectableChildren() {
            return List.of();
         }
      }
   }
}
