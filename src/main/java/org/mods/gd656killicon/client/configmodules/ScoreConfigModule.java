package org.mods.gd656killicon.client.configmodules;

import net.minecraft.text.Text;
import org.mods.gd656killicon.Config;
import org.mods.gd656killicon.client.ElementConfigScreen;

public class ScoreConfigModule {
   private final ElementConfigScreen.OptionsList list;
   public ElementConfigScreen.OptionsList.EditBoxOptionEntry flashingColor;
   public ElementConfigScreen.OptionsList.EditBoxOptionEntry highScoreColor;

   public ScoreConfigModule(ElementConfigScreen.OptionsList list) {
      this.list = list;
   }

   public void addConfigOptions() {
      this.list.addEntryPublic(new ElementConfigScreen.OptionsList.SpacerEntry());
      this.list.addEntryPublic(new ElementConfigScreen.OptionsList.CategoryEntry(Text.literal("分数字幕私有配置")));
      this.addSpacerAndEntry(
         this.flashingColor = new ElementConfigScreen.OptionsList.EditBoxOptionEntry(Text.literal("积分闪烁颜色"), Config.FLASHING_COLOR, 100, true)
      );
      this.addSpacerAndEntry(
         this.highScoreColor = new ElementConfigScreen.OptionsList.EditBoxOptionEntry(Text.literal("积分高分颜色"), Config.HIGH_SCORE_COLOR, 100, true)
      );
   }

   public void saveConfig() {
      if (this.flashingColor != null) {
         Config.getInstance();
         Config.FLASHING_COLOR = this.flashingColor.getValue();
      }

      if (this.highScoreColor != null) {
         Config.getInstance();
         Config.HIGH_SCORE_COLOR = this.highScoreColor.getValue();
      }

      Config.saveConfig();
      Config.loadRuntimeVariables();
   }

   private void addSpacerAndEntry(ElementConfigScreen.OptionsList.Entry entry) {
      this.list.addEntryPublic(new ElementConfigScreen.OptionsList.SpacerEntry());
      this.list.addEntryPublic(entry);
   }
}
