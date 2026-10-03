package org.mods.gd656killicon.client;

import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.MouseInput;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.mods.gd656killicon.Config;
import org.mods.gd656killicon.client.configmodules.BonusConfigModule;
import org.mods.gd656killicon.client.configmodules.CardIconConfigModule;
import org.mods.gd656killicon.client.configmodules.ComboKillIconConfigModule;
import org.mods.gd656killicon.client.configmodules.KillIconConfigModule;
import org.mods.gd656killicon.client.configmodules.ScoreConfigModule;
import org.mods.gd656killicon.client.configmodules.SubtitleConfigModule;

public class ElementConfigScreen extends Screen {
   private final Screen parent;
   private final ModConfigScreen.DraggableElement element;
   private ElementConfigScreen.OptionsList list;
   public ElementConfigScreen.OptionsList.BooleanOptionEntry elementVisible;
   public ElementConfigScreen.OptionsList.SliderOptionEntry elementSize;
   private KillIconConfigModule killIconConfigModule;
   private SubtitleConfigModule subtitleConfigModule;
   private ScoreConfigModule scoreConfigModule;
   private BonusConfigModule bonusConfigModule;
   private CardIconConfigModule cardIconConfigModule;
   private ComboKillIconConfigModule comboKillIconConfigModule;
   private static final int ITEM_HEIGHT = 15;
   private static final int SLIDER_WIDTH = 100;
   private static final int SLIDER_HEIGHT = 20;

   public ElementConfigScreen(Screen parent, ModConfigScreen.DraggableElement element) {
      super(Text.translatable("gd656killicon.elementconfig.title"));
      this.parent = parent;
      this.element = element;
   }

   protected void init() {
      super.init();
      int listTop = 32;
      int listBottom = this.height - 64;
      int listHeight = listBottom - listTop;
      this.list = new ElementConfigScreen.OptionsList(this.client, this.width, listHeight, listTop, 15);
      this.addSelectableChild(this.list);
      this.list.addEntryPublic(new ElementConfigScreen.OptionsList.CategoryEntry(Text.literal("元素配置: " + this.element.name)));
      this.list.addEntryPublic(new ElementConfigScreen.OptionsList.SpacerEntry());
      int minSize = 16;
      int maxSize = 500;
      if (this.element.type == ModConfigScreen.ElementType.ICON) {
         maxSize = 96;
      } else if (this.element.type == ModConfigScreen.ElementType.SUBTITLE || this.element.type == ModConfigScreen.ElementType.BONUS) {
         minSize = 50;
         maxSize = 200;
      } else if (this.element.type == ModConfigScreen.ElementType.SCORE) {
         minSize = 100;
      }

      this.elementSize = new ElementConfigScreen.OptionsList.SliderOptionEntry(
         Text.literal("元素大小"), minSize, maxSize, this.getCurrentElementSize(this.element.type)
      );
      this.list.addEntryPublic(this.elementSize);
      this.list.addEntryPublic(new ElementConfigScreen.OptionsList.SpacerEntry());
      this.elementVisible = new ElementConfigScreen.OptionsList.BooleanOptionEntry(Text.literal("游戏中显示"), this.getCurrentElementVisible(this.element.type));
      this.list.addEntryPublic(this.elementVisible);
      if (this.element.type == ModConfigScreen.ElementType.ICON) {
         this.addPrivateConfigOptions();
      } else if (this.element.type == ModConfigScreen.ElementType.SUBTITLE) {
         this.addSubtitleConfigOptions();
      } else if (this.element.type == ModConfigScreen.ElementType.SCORE) {
         this.addScoreConfigOptions();
      } else if (this.element.type == ModConfigScreen.ElementType.BONUS) {
         this.addBonusConfigOptions();
      }

      int buttonWidth = 100;
      int buttonSpacing = 10;
      int totalWidth = buttonWidth * 2 + buttonSpacing;
      int startX = (this.width - totalWidth) / 2;
      int buttonY = this.height - 30;
      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> {
         this.saveConfig();
         if (this.client != null) {
            this.client.setScreen(this.parent);
         }
      }).dimensions(startX, buttonY, buttonWidth, 20).build());
      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> {
         if (this.client != null) {
            this.client.setScreen(this.parent);
         }
      }).dimensions(startX + buttonWidth + buttonSpacing, buttonY, buttonWidth, 20).build());
   }

   public void render(@NotNull DrawContext guiGraphics, int mouseX, int mouseY, float partialTick) {
      guiGraphics.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 16777215);
      if (this.element.type == ModConfigScreen.ElementType.ICON) {
         String sharedText = switch (Config.iconMode) {
            case SCROLLING -> "滚动模式专用配置";
            case COMBO -> "连杀模式专用配置";
            case CARD -> "卡牌模式专用配置";
         };
         guiGraphics.drawText(this.textRenderer, sharedText, 10, this.height - 9 - 10, 8947848, true);
      }

      if (this.element.type == ModConfigScreen.ElementType.SUBTITLE
         || this.element.type == ModConfigScreen.ElementType.SCORE
         || this.element.type == ModConfigScreen.ElementType.BONUS) {
         guiGraphics.drawText(this.textRenderer, "三模式共用", 10, this.height - 9 - 10, 8947848, true);
      }

      if (this.list != null) {
         this.list.render(guiGraphics, mouseX, mouseY, partialTick);
      }

      super.render(guiGraphics, mouseX, mouseY, partialTick);
   }

   public void close() {
      if (this.client != null) {
         this.client.setScreen(this.parent);
      }
   }

   private void addPrivateConfigOptions() {
      if (this.element.type == ModConfigScreen.ElementType.ICON) {
         switch (Config.iconMode) {
            case SCROLLING:
            default:
               this.killIconConfigModule = new KillIconConfigModule(this.list);
               this.killIconConfigModule.addConfigOptions();
               break;
            case COMBO:
               this.comboKillIconConfigModule = new ComboKillIconConfigModule(this.list);
               this.comboKillIconConfigModule.addConfigOptions();
               break;
            case CARD:
               this.cardIconConfigModule = new CardIconConfigModule(this.list);
               this.cardIconConfigModule.addConfigOptions();
         }
      }
   }

   private void addSubtitleConfigOptions() {
      this.subtitleConfigModule = new SubtitleConfigModule(this.list);
      this.subtitleConfigModule.addConfigOptions();
   }

   private void addScoreConfigOptions() {
      this.scoreConfigModule = new ScoreConfigModule(this.list);
      this.scoreConfigModule.addConfigOptions();
   }

   private void addBonusConfigOptions() {
      this.bonusConfigModule = new BonusConfigModule(this.list);
      this.bonusConfigModule.addConfigOptions();
   }

   private int getCurrentElementSize(ModConfigScreen.ElementType type) {
      if (type == ModConfigScreen.ElementType.ICON) {
         return switch (Config.iconMode) {
            case SCROLLING -> Config.scrollIconSize;
            case COMBO -> Config.comboIconSize;
            case CARD -> Config.cardIconSize;
         };
      } else if (type == ModConfigScreen.ElementType.SUBTITLE) {
         return switch (Config.iconMode) {
            case SCROLLING -> Config.scrollSubtitleSize;
            case COMBO -> Config.comboSubtitleSize;
            case CARD -> Config.cardSubtitleSize;
         };
      } else if (type == ModConfigScreen.ElementType.SCORE) {
         return switch (Config.iconMode) {
            case SCROLLING -> Config.scrollScoreSize;
            case COMBO -> Config.comboScoreSize;
            case CARD -> Config.cardScoreSize;
         };
      } else if (type == ModConfigScreen.ElementType.BONUS) {
         return switch (Config.iconMode) {
            case SCROLLING -> Config.scrollBonusSize;
            case COMBO -> Config.comboBonusSize;
            case CARD -> Config.cardBonusSize;
         };
      } else {
         return type == ModConfigScreen.ElementType.BOTTOMBAR ? Config.cardBottombarSize : 100;
      }
   }

   private boolean getCurrentElementVisible(ModConfigScreen.ElementType type) {
      if (type == ModConfigScreen.ElementType.ICON) {
         return switch (Config.iconMode) {
            case SCROLLING -> Config.scrollIconVisible;
            case COMBO -> Config.comboIconVisible;
            case CARD -> Config.cardIconVisible;
         };
      } else if (type == ModConfigScreen.ElementType.SUBTITLE) {
         return switch (Config.iconMode) {
            case SCROLLING -> Config.scrollSubtitleVisible;
            case COMBO -> Config.comboSubtitleVisible;
            case CARD -> Config.cardSubtitleVisible;
         };
      } else if (type == ModConfigScreen.ElementType.SCORE) {
         return switch (Config.iconMode) {
            case SCROLLING -> Config.scrollScoreVisible;
            case COMBO -> Config.comboScoreVisible;
            case CARD -> Config.cardScoreVisible;
         };
      } else if (type == ModConfigScreen.ElementType.BONUS) {
         return switch (Config.iconMode) {
            case SCROLLING -> Config.scrollBonusVisible;
            case COMBO -> Config.comboBonusVisible;
            case CARD -> Config.cardBonusVisible;
         };
      } else {
         return true;
      }
   }

   private void saveConfig() {
      int newSize = this.elementSize.getValue();
      boolean newVisible = this.elementVisible.getValue();
      Config config = Config.getInstance();
      label57:
      switch (this.element.type) {
         case ICON:
            switch (Config.iconMode) {
               case SCROLLING:
                  config.SCROLL_ICON_SIZE = newSize;
                  config.SCROLL_ICON_VISIBLE = newVisible;
                  Config.scrollIconSize = newSize;
                  Config.scrollIconVisible = newVisible;
                  if (this.killIconConfigModule != null) {
                     this.killIconConfigModule.saveConfig();
                  }
                  break label57;
               case COMBO:
                  config.COMBO_ICON_SIZE = newSize;
                  config.COMBO_ICON_VISIBLE = newVisible;
                  Config.comboIconSize = newSize;
                  Config.comboIconVisible = newVisible;
                  if (this.comboKillIconConfigModule != null) {
                     this.comboKillIconConfigModule.saveConfig();
                  }
                  break label57;
               case CARD:
                  config.CARD_ICON_SIZE = newSize;
                  config.CARD_ICON_VISIBLE = newVisible;
                  Config.cardIconSize = newSize;
                  Config.cardIconVisible = newVisible;
                  if (this.cardIconConfigModule != null) {
                     this.cardIconConfigModule.saveConfig();
                     if (this.cardIconConfigModule.bottombarVisibleEntry != null && this.cardIconConfigModule.bottombarSizeEntry != null) {
                        config.CARD_BOTTOMBAR_VISIBLE = this.cardIconConfigModule.bottombarVisibleEntry.getValue();
                        config.CARD_BOTTOMBAR_SIZE = this.cardIconConfigModule.bottombarSizeEntry.getValue();
                        Config.cardBottombarVisible = this.cardIconConfigModule.bottombarVisibleEntry.getValue();
                        Config.cardBottombarSize = this.cardIconConfigModule.bottombarSizeEntry.getValue();
                     }
                  }
               default:
                  break label57;
            }
         case SUBTITLE:
            switch (Config.iconMode) {
               case SCROLLING:
                  config.SCROLL_SUBTITLE_SIZE = newSize;
                  config.SCROLL_SUBTITLE_VISIBLE = newVisible;
                  Config.scrollSubtitleSize = newSize;
                  Config.scrollSubtitleVisible = newVisible;
                  break;
               case COMBO:
                  config.COMBO_SUBTITLE_SIZE = newSize;
                  config.COMBO_SUBTITLE_VISIBLE = newVisible;
                  Config.comboSubtitleSize = newSize;
                  Config.comboSubtitleVisible = newVisible;
                  break;
               case CARD:
                  config.CARD_SUBTITLE_SIZE = newSize;
                  config.CARD_SUBTITLE_VISIBLE = newVisible;
                  Config.cardSubtitleSize = newSize;
                  Config.cardSubtitleVisible = newVisible;
            }

            if (this.subtitleConfigModule != null) {
               this.subtitleConfigModule.saveConfig();
            }
            break;
         case SCORE:
            switch (Config.iconMode) {
               case SCROLLING:
                  config.SCROLL_SCORE_SIZE = newSize;
                  config.SCROLL_SCORE_VISIBLE = newVisible;
                  Config.scrollScoreSize = newSize;
                  Config.scrollScoreVisible = newVisible;
                  break;
               case COMBO:
                  config.COMBO_SCORE_SIZE = newSize;
                  config.COMBO_SCORE_VISIBLE = newVisible;
                  Config.comboScoreSize = newSize;
                  Config.comboScoreVisible = newVisible;
                  break;
               case CARD:
                  config.CARD_SCORE_SIZE = newSize;
                  config.CARD_SCORE_VISIBLE = newVisible;
                  Config.cardScoreSize = newSize;
                  Config.cardScoreVisible = newVisible;
            }

            if (this.scoreConfigModule != null) {
               this.scoreConfigModule.saveConfig();
            }
            break;
         case BONUS:
            switch (Config.iconMode) {
               case SCROLLING:
                  config.SCROLL_BONUS_SIZE = newSize;
                  config.SCROLL_BONUS_VISIBLE = newVisible;
                  Config.scrollBonusSize = newSize;
                  Config.scrollBonusVisible = newVisible;
                  break;
               case COMBO:
                  config.COMBO_BONUS_SIZE = newSize;
                  config.COMBO_BONUS_VISIBLE = newVisible;
                  Config.comboBonusSize = newSize;
                  Config.comboBonusVisible = newVisible;
                  break;
               case CARD:
                  config.CARD_BONUS_SIZE = newSize;
                  config.CARD_BONUS_VISIBLE = newVisible;
                  Config.cardBonusSize = newSize;
                  Config.cardBonusVisible = newVisible;
            }

            if (this.bonusConfigModule != null) {
               this.bonusConfigModule.saveConfig();
            }
      }

      Config.saveConfig();
      Config.loadConfig();
      if (this.parent instanceof ModConfigScreen) {
         ((ModConfigScreen)this.parent).updateElementSizes();
      }
   }

   public static class OptionsList extends ElementListWidget<ElementConfigScreen.OptionsList.Entry> {
      public OptionsList(MinecraftClient minecraft, int width, int height, int y0, int itemHeight) {
         super(minecraft, width, height, y0, itemHeight);
      }

      public void addEntryPublic(ElementConfigScreen.OptionsList.Entry entry) {
         this.addEntry(entry);
      }

      public static class BooleanOptionEntry extends ElementConfigScreen.OptionsList.Entry {
         private final CheckboxWidget checkbox;
         private final Text label;

         public BooleanOptionEntry(Text label, boolean initialValue) {
            this.label = label;
            this.checkbox = CheckboxWidget.builder(Text.empty(), MinecraftClient.getInstance().textRenderer).pos(0, 0).checked(initialValue).build();
         }

         public void render(DrawContext guiGraphics, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            int top = this.getY();
            int left = this.getX();
            int width = this.getWidth();
            guiGraphics.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, this.label, left + 10, top + 5, 16777215);
            this.checkbox.setX(left + width - 30);
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

      public static class CategoryEntry extends ElementConfigScreen.OptionsList.Entry {
         private final Text text;

         public CategoryEntry(Text text) {
            this.text = text;
         }

         public void render(DrawContext guiGraphics, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            int top = this.getY();
            int left = this.getX();
            int width = this.getWidth();
            guiGraphics.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, this.text, left + width / 2, top + 5, 16777215);
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

      public static class EditBoxOptionEntry extends ElementConfigScreen.OptionsList.Entry {
         private final TextFieldWidget editBox;
         private final Text label;
         private final boolean isColorOption;

         public EditBoxOptionEntry(Text label, String initialValue, int width) {
            this(label, initialValue, width, false);
         }

         public EditBoxOptionEntry(Text label, String initialValue, int width, boolean isColorOption) {
            this.label = label;
            this.isColorOption = isColorOption;
            this.editBox = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, width, 20, Text.empty());
            this.editBox.setText(initialValue);
            this.editBox.setMaxLength(isColorOption ? 6 : 256);
            this.editBox.setFocused(true);
            this.editBox.setFocusUnlocked(true);
         }

         public void render(DrawContext guiGraphics, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            int top = this.getY();
            int left = this.getX();
            int width = this.getWidth();
            guiGraphics.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, this.label, left + 10, top + 5, 16777215);
            if (this.isColorOption) {
               this.editBox.setX(left + width - 150 + 75);
               this.editBox.setY(top - 2);
               this.editBox.setWidth(75);
               this.editBox.render(guiGraphics, mouseX, mouseY, partialTick);
               int colorBoxX = left + width - 150 + 130;
               int colorBoxSize = 20;
               int colorBoxY = top - 2;
               String colorText = this.editBox.getText();

               int colorValue;
               try {
                  String colorStr = "FF" + colorText.replace("#", "");
                  colorValue = (int)Long.parseLong(colorStr, 16);
               } catch (Exception var17) {
                  colorValue = -1;
               }

               guiGraphics.fill(colorBoxX, colorBoxY, colorBoxX + colorBoxSize, colorBoxY + colorBoxSize, colorValue);
               RenderHelper.drawBorder(guiGraphics, colorBoxX, colorBoxY, colorBoxSize, colorBoxSize, -16777216);
            } else {
               this.editBox.setX(left + width - 150 + 50);
               this.editBox.setY(top - 2);
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

      public abstract static class Entry extends ElementListWidget.Entry<ElementConfigScreen.OptionsList.Entry> {
      }

      public static class IconStyleOptionEntry extends ElementConfigScreen.OptionsList.Entry {
         private final CyclingButtonWidget<Config.IconStyle> styleButton;
         private final Text label;

         public IconStyleOptionEntry(Text label, Config.IconStyle initialValue) {
            this.label = label;
            this.styleButton = CyclingButtonWidget.<Config.IconStyle>builder(style -> Text.translatable(style.getTranslationKey()), initialValue)
               .values(Config.IconStyle.values())
               .build(0, 0, 100, 20, Text.empty());
         }

         public void render(DrawContext guiGraphics, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            int top = this.getY();
            int left = this.getX();
            int width = this.getWidth();
            guiGraphics.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, this.label, left + 10, top + 5, 16777215);
            this.styleButton.setX(left + width - 100 - 10);
            this.styleButton.setY(top - 2);
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

      public static class SliderOptionEntry extends ElementConfigScreen.OptionsList.Entry {
         private final ElementConfigScreen.OptionsList.SliderOptionEntry.CustomSlider slider;
         private final Text label;

         public SliderOptionEntry(Text label, int min, int max, int initialValue) {
            this.label = label;
            this.slider = new ElementConfigScreen.OptionsList.SliderOptionEntry.CustomSlider(
               0, 0, 100, 20, Text.empty(), (double)(initialValue - min) / (max - min), min, max
            );
         }

         public void render(DrawContext guiGraphics, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            int top = this.getY();
            int left = this.getX();
            int width = this.getWidth();
            guiGraphics.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, this.label, left + 10, top + 5, 16777215);
            this.slider.setX(left + width - 100 - 10);
            this.slider.setY(top - 2);
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

         public static class CustomSlider extends SliderWidget {
            private final int min;
            private final int max;

            public CustomSlider(int x, int y, int width, int height, Text message, double value, int min, int max) {
               super(x, y, width, height, message, value);
               this.min = min;
               this.max = max;
               this.updateMessage();
            }

            protected void updateMessage() {
               this.setMessage(Text.literal(String.valueOf(this.getIntValue())));
            }

            protected void applyValue() {
            }

            public int getIntValue() {
               return this.min + (int)(this.value * (this.max - this.min));
            }
         }
      }

      public static class SpacerEntry extends ElementConfigScreen.OptionsList.Entry {
         public void render(@NotNull DrawContext guiGraphics, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
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
