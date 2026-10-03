package org.mods.gd656killicon.client.configmodules;

import net.minecraft.text.Text;
import org.mods.gd656killicon.Config;
import org.mods.gd656killicon.client.ElementConfigScreen;

public class SubtitleConfigModule {
   private final ElementConfigScreen.OptionsList list;
   public ElementConfigScreen.OptionsList.EditBoxOptionEntry assistSubtitleFormat;
   public ElementConfigScreen.OptionsList.EditBoxOptionEntry criticalColor;
   public ElementConfigScreen.OptionsList.EditBoxOptionEntry customSubtitleFormat;
   public ElementConfigScreen.OptionsList.EditBoxOptionEntry targetColor;
   public ElementConfigScreen.OptionsList.EditBoxOptionEntry weaponColor;

   public SubtitleConfigModule(ElementConfigScreen.OptionsList list) {
      this.list = list;
   }

   public void addConfigOptions() {
      this.list.addEntryPublic(new ElementConfigScreen.OptionsList.SpacerEntry());
      this.list.addEntryPublic(new ElementConfigScreen.OptionsList.CategoryEntry(Text.literal("字幕私有配置")));
      this.addSpacerAndEntry(
         this.assistSubtitleFormat = new ElementConfigScreen.OptionsList.EditBoxOptionEntry(Text.literal("自定义助攻字幕"), Config.assistSubtitleFormat, 200)
      );
      this.addSpacerAndEntry(
         this.customSubtitleFormat = new ElementConfigScreen.OptionsList.EditBoxOptionEntry(Text.literal("自定义淘汰字幕"), Config.customSubtitleFormat, 200)
      );
      this.addSpacerAndEntry(
         this.criticalColor = new ElementConfigScreen.OptionsList.EditBoxOptionEntry(Text.literal("暴击文字颜色"), Config.CRITICAL_COLOR, 100, true)
      );
      this.addSpacerAndEntry(
         this.targetColor = new ElementConfigScreen.OptionsList.EditBoxOptionEntry(Text.literal("目标文字颜色"), Config.TARGET_COLOR, 100, true)
      );
      this.addSpacerAndEntry(
         this.weaponColor = new ElementConfigScreen.OptionsList.EditBoxOptionEntry(Text.literal("武器文字颜色"), Config.WEAPON_COLOR, 100, true)
      );
   }

   public void saveConfig() {
      if (this.assistSubtitleFormat != null) {
         Config.getInstance().ASSIST_SUBTITLE_FORMAT = this.assistSubtitleFormat.getValue();
      }

      if (this.criticalColor != null) {
         Config.getInstance();
         Config.CRITICAL_COLOR = this.criticalColor.getValue();
      }

      if (this.customSubtitleFormat != null) {
         Config.getInstance().CUSTOM_SUBTITLE_FORMAT = this.customSubtitleFormat.getValue();
      }

      if (this.targetColor != null) {
         Config.getInstance();
         Config.TARGET_COLOR = this.targetColor.getValue();
      }

      if (this.weaponColor != null) {
         Config.getInstance();
         Config.WEAPON_COLOR = this.weaponColor.getValue();
      }

      Config.saveConfig();
      Config.loadRuntimeVariables();
   }

   private void addSpacerAndEntry(ElementConfigScreen.OptionsList.Entry entry) {
      this.list.addEntryPublic(new ElementConfigScreen.OptionsList.SpacerEntry());
      this.list.addEntryPublic(entry);
   }
}
