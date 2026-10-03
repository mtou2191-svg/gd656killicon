package org.mods.gd656killicon.client.configmodules;

import net.minecraft.text.Text;
import org.mods.gd656killicon.Config;
import org.mods.gd656killicon.client.ElementConfigScreen;

public class KillIconConfigModule {
   private final ElementConfigScreen.OptionsList list;
   public ElementConfigScreen.OptionsList.SliderOptionEntry animationDuration;
   public ElementConfigScreen.OptionsList.SliderOptionEntry forceHideCount;
   public ElementConfigScreen.OptionsList.SliderOptionEntry iconDuration;
   public ElementConfigScreen.OptionsList.SliderOptionEntry iconSpacing;
   public ElementConfigScreen.OptionsList.SliderOptionEntry maxDisplayCount;
   public ElementConfigScreen.OptionsList.IconStyleOptionEntry iconStyle;

   public KillIconConfigModule(ElementConfigScreen.OptionsList list) {
      this.list = list;
   }

   public void addConfigOptions() {
      this.list.addEntryPublic(new ElementConfigScreen.OptionsList.SpacerEntry());
      this.list.addEntryPublic(new ElementConfigScreen.OptionsList.CategoryEntry(Text.literal("滚动模式私有配置")));
      this.addSpacerAndEntry(
         this.animationDuration = new ElementConfigScreen.OptionsList.SliderOptionEntry(
            Text.literal("图标动画执行时间"), 100, 2000, Config.killIconAnimationDuration
         )
      );
      this.addSpacerAndEntry(
         this.forceHideCount = new ElementConfigScreen.OptionsList.SliderOptionEntry(Text.literal("图标强制隐藏数量"), 3, 100, Config.forceHideCount)
      );
      this.addSpacerAndEntry(
         this.iconDuration = new ElementConfigScreen.OptionsList.SliderOptionEntry(Text.literal("图标显示时长"), 20, 200, Config.killIconDuration)
      );
      this.addSpacerAndEntry(
         this.iconSpacing = new ElementConfigScreen.OptionsList.SliderOptionEntry(Text.literal("图标间隔"), 10, 100, Config.killIconSpacing)
      );
      this.addSpacerAndEntry(this.iconStyle = new ElementConfigScreen.OptionsList.IconStyleOptionEntry(Text.literal("图标风格预设"), Config.iconStyle));
      this.addSpacerAndEntry(
         this.maxDisplayCount = new ElementConfigScreen.OptionsList.SliderOptionEntry(Text.literal("图标最大显示数量"), 5, 100, Config.maxDisplayCount)
      );
   }

   public void saveConfig() {
      if (this.animationDuration != null) {
         Config.getInstance().KILL_ICON_ANIMATION_DURATION = this.animationDuration.getValue();
      }

      if (this.forceHideCount != null) {
         Config.getInstance().FORCE_HIDE_COUNT = this.forceHideCount.getValue();
      }

      if (this.iconDuration != null) {
         Config.getInstance().KILL_ICON_DURATION = this.iconDuration.getValue();
      }

      if (this.iconSpacing != null) {
         Config.getInstance().KILL_ICON_SPACING = this.iconSpacing.getValue();
      }

      if (this.iconStyle != null) {
         Config.getInstance().ICON_STYLE = this.iconStyle.getValue();
      }

      if (this.maxDisplayCount != null) {
         Config.getInstance().MAX_DISPLAY_COUNT = this.maxDisplayCount.getValue();
      }

      Config.saveConfig();
      Config.loadRuntimeVariables();
   }

   private void addSpacerAndEntry(ElementConfigScreen.OptionsList.Entry entry) {
      this.list.addEntryPublic(new ElementConfigScreen.OptionsList.SpacerEntry());
      this.list.addEntryPublic(entry);
   }
}
