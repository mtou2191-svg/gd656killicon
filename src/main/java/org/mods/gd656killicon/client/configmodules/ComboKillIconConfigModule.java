package org.mods.gd656killicon.client.configmodules;

import net.minecraft.text.Text;
import org.mods.gd656killicon.Config;
import org.mods.gd656killicon.client.ElementConfigScreen;

public class ComboKillIconConfigModule {
   private final ElementConfigScreen.OptionsList list;
   public ElementConfigScreen.OptionsList.SliderOptionEntry animationDuration;
   public ElementConfigScreen.OptionsList.SliderOptionEntry animationTotalDuration;
   public ElementConfigScreen.OptionsList.SliderOptionEntry animationTotalFrames;
   public ElementConfigScreen.OptionsList.BooleanOptionEntry animationEnabled;
   public ElementConfigScreen.OptionsList.IconStyleOptionEntry iconStyle;

   public ComboKillIconConfigModule(ElementConfigScreen.OptionsList list) {
      this.list = list;
   }

   public void addConfigOptions() {
      this.list.addEntryPublic(new ElementConfigScreen.OptionsList.SpacerEntry());
      this.list.addEntryPublic(new ElementConfigScreen.OptionsList.CategoryEntry(Text.literal("连杀模式私有配置")));
      this.addSpacerAndEntry(
         this.animationEnabled = new ElementConfigScreen.OptionsList.BooleanOptionEntry(Text.literal("启用图标动图"), Config.comboIconAnimationEnabled)
      );
      this.addSpacerAndEntry(
         this.animationTotalDuration = new ElementConfigScreen.OptionsList.SliderOptionEntry(
            Text.literal("图标动画显示时长"), 500, 5000, Config.comboIconAnimationDuration
         )
      );
      this.addSpacerAndEntry(
         this.animationTotalFrames = new ElementConfigScreen.OptionsList.SliderOptionEntry(
            Text.literal("图标动画帧数"), 10, 60, Config.comboIconAnimationTotalFrames
         )
      );
      this.addSpacerAndEntry(
         this.animationDuration = new ElementConfigScreen.OptionsList.SliderOptionEntry(
            Text.literal("图标动画执行时间"), 100, 2000, Config.killIconAnimationDuration
         )
      );
      this.addSpacerAndEntry(this.iconStyle = new ElementConfigScreen.OptionsList.IconStyleOptionEntry(Text.literal("图标风格预设"), Config.iconStyle));
   }

   public void saveConfig() {
      if (this.animationEnabled != null) {
         Config.getInstance().COMBO_ICON_ANIMATION_ENABLED = this.animationEnabled.getValue();
      }

      if (this.animationTotalDuration != null) {
         Config.getInstance().COMBO_ICON_ANIMATION_DURATION = this.animationTotalDuration.getValue();
      }

      if (this.animationTotalFrames != null) {
         Config.getInstance().COMBO_ICON_ANIMATION_TOTAL_FRAMES = this.animationTotalFrames.getValue();
      }

      if (this.animationDuration != null) {
         Config.getInstance().KILL_ICON_ANIMATION_DURATION = this.animationDuration.getValue();
      }

      if (this.iconStyle != null) {
         Config.getInstance().ICON_STYLE = this.iconStyle.getValue();
      }

      Config.saveConfig();
      Config.loadRuntimeVariables();
   }

   private void addSpacerAndEntry(ElementConfigScreen.OptionsList.Entry entry) {
      this.list.addEntryPublic(new ElementConfigScreen.OptionsList.SpacerEntry());
      this.list.addEntryPublic(entry);
   }
}
