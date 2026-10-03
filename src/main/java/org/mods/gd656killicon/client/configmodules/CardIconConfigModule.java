package org.mods.gd656killicon.client.configmodules;

import net.minecraft.text.Text;
import org.mods.gd656killicon.Config;
import org.mods.gd656killicon.client.ElementConfigScreen;
import org.mods.gd656killicon.client.iconrenderer.CardModeRenderer;

public class CardIconConfigModule {
   private final ElementConfigScreen.OptionsList list;
   public ElementConfigScreen.OptionsList.IconStyleOptionEntry cardIconStyle;
   public ElementConfigScreen.OptionsList.BooleanOptionEntry bottombarVisibleEntry;
   public ElementConfigScreen.OptionsList.SliderOptionEntry bottombarSizeEntry;
   public ElementConfigScreen.OptionsList.SliderOptionEntry bottombarOffsetYEntry;
   public ElementConfigScreen.OptionsList.SliderOptionEntry killChainTimeoutEntry;

   public CardIconConfigModule(ElementConfigScreen.OptionsList list) {
      this.list = list;
   }

   public void addConfigOptions() {
      this.list.addEntryPublic(new ElementConfigScreen.OptionsList.CategoryEntry(Text.literal("卡牌配置")));
      this.addSpacerAndEntry(this.cardIconStyle = new ElementConfigScreen.OptionsList.IconStyleOptionEntry(Text.literal("卡牌图标风格预设"), Config.cardIconStyle));
      this.addSpacerAndEntry(
         this.killChainTimeoutEntry = new ElementConfigScreen.OptionsList.SliderOptionEntry(
            Text.literal("连杀超时时间(秒)"), 0, 60, CardModeRenderer.getKillChainTimeout() / 1000
         )
      );
      this.list.addEntryPublic(new ElementConfigScreen.OptionsList.SpacerEntry());
      this.list.addEntryPublic(new ElementConfigScreen.OptionsList.CategoryEntry(Text.literal("底部栏配置")));
      this.addSpacerAndEntry(
         this.bottombarVisibleEntry = new ElementConfigScreen.OptionsList.BooleanOptionEntry(Text.literal("显示底部栏"), Config.cardBottombarVisible)
      );
      this.addSpacerAndEntry(
         this.bottombarSizeEntry = new ElementConfigScreen.OptionsList.SliderOptionEntry(Text.literal("底部栏大小"), 50, 200, Config.cardBottombarSize)
      );
      this.addSpacerAndEntry(
         this.bottombarOffsetYEntry = new ElementConfigScreen.OptionsList.SliderOptionEntry(
            Text.literal("底部栏垂直偏移"), -100, 100, Config.cardBottombarOffsetY
         )
      );
   }

   public void saveConfig() {
      if (this.cardIconStyle != null) {
         Config.getInstance().CARD_ICON_STYLE = this.cardIconStyle.getValue();
      }

      if (this.killChainTimeoutEntry != null) {
         CardModeRenderer.setKillChainTimeout(this.killChainTimeoutEntry.getValue() * 1000);
      }

      if (this.bottombarVisibleEntry != null) {
         Config.getInstance().CARD_BOTTOMBAR_VISIBLE = this.bottombarVisibleEntry.getValue();
      }

      if (this.bottombarSizeEntry != null) {
         Config.getInstance().CARD_BOTTOMBAR_SIZE = this.bottombarSizeEntry.getValue();
      }

      if (this.bottombarOffsetYEntry != null) {
         Config.getInstance().CARD_BOTTOMBAR_OFFSET_Y = this.bottombarOffsetYEntry.getValue();
      }

      Config.saveConfig();
      Config.loadRuntimeVariables();
   }

   private void addSpacerAndEntry(ElementConfigScreen.OptionsList.Entry entry) {
      this.list.addEntryPublic(new ElementConfigScreen.OptionsList.SpacerEntry());
      this.list.addEntryPublic(entry);
   }
}
