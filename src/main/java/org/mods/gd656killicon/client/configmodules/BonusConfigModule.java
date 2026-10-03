package org.mods.gd656killicon.client.configmodules;

import net.minecraft.text.Text;
import org.mods.gd656killicon.Config;
import org.mods.gd656killicon.client.ElementConfigScreen;

public class BonusConfigModule {
   private final ElementConfigScreen.OptionsList list;
   public ElementConfigScreen.OptionsList.EditBoxOptionEntry assistBonusDisplay;
   public ElementConfigScreen.OptionsList.EditBoxOptionEntry comboBonusDisplay;
   public ElementConfigScreen.OptionsList.EditBoxOptionEntry criticalBonusDisplay;
   public ElementConfigScreen.OptionsList.EditBoxOptionEntry damageBonusDisplay;
   public ElementConfigScreen.OptionsList.EditBoxOptionEntry handBonusDisplay;
   public ElementConfigScreen.OptionsList.EditBoxOptionEntry killBonusDisplay;
   public ElementConfigScreen.OptionsList.EditBoxOptionEntry longrangeBonusDisplay;
   public ElementConfigScreen.OptionsList.EditBoxOptionEntry magicBonusDisplay;
   public ElementConfigScreen.OptionsList.SliderOptionEntry bonusLineSpacing;

   public BonusConfigModule(ElementConfigScreen.OptionsList list) {
      this.list = list;
   }

   public void addConfigOptions() {
      this.list.addEntryPublic(new ElementConfigScreen.OptionsList.SpacerEntry());
      this.list.addEntryPublic(new ElementConfigScreen.OptionsList.CategoryEntry(Text.literal("加分项字幕私有配置")));
      this.addSpacerAndEntry(
         this.assistBonusDisplay = new ElementConfigScreen.OptionsList.EditBoxOptionEntry(Text.literal("助攻击败显示名称"), Config.assistBonusDisplay, 200)
      );
      this.addSpacerAndEntry(
         this.bonusLineSpacing = new ElementConfigScreen.OptionsList.SliderOptionEntry(Text.literal("字幕行间距"), 1, 60, Config.bonusLineSpacing)
      );
      this.addSpacerAndEntry(
         this.comboBonusDisplay = new ElementConfigScreen.OptionsList.EditBoxOptionEntry(Text.literal("连杀显示名称"), Config.comboBonusDisplay, 200)
      );
      this.addSpacerAndEntry(
         this.criticalBonusDisplay = new ElementConfigScreen.OptionsList.EditBoxOptionEntry(Text.literal("暴击加成显示名称"), Config.criticalBonusDisplay, 200)
      );
      this.addSpacerAndEntry(
         this.damageBonusDisplay = new ElementConfigScreen.OptionsList.EditBoxOptionEntry(Text.literal("造成伤害显示名称"), Config.damageBonusDisplay, 200)
      );
      this.addSpacerAndEntry(
         this.handBonusDisplay = new ElementConfigScreen.OptionsList.EditBoxOptionEntry(Text.literal("空手攻击显示名称"), Config.handBonusDisplay, 200)
      );
      this.addSpacerAndEntry(
         this.killBonusDisplay = new ElementConfigScreen.OptionsList.EditBoxOptionEntry(Text.literal("击败生物显示名称"), Config.killBonusDisplay, 200)
      );
      this.addSpacerAndEntry(
         this.longrangeBonusDisplay = new ElementConfigScreen.OptionsList.EditBoxOptionEntry(Text.literal("远距离击败显示名称"), Config.longrangeBonusDisplay, 200)
      );
      this.addSpacerAndEntry(
         this.magicBonusDisplay = new ElementConfigScreen.OptionsList.EditBoxOptionEntry(Text.literal("魔法伤害显示名称"), Config.magicBonusDisplay, 200)
      );
   }

   public void saveConfig() {
      if (this.assistBonusDisplay != null) {
         Config.getInstance().ASSIST_BONUS_DISPLAY = this.assistBonusDisplay.getValue();
      }

      if (this.bonusLineSpacing != null) {
         Config.getInstance().BONUS_LINE_SPACING = this.bonusLineSpacing.getValue();
      }

      if (this.comboBonusDisplay != null) {
         Config.getInstance().COMBO_BONUS_DISPLAY = this.comboBonusDisplay.getValue();
      }

      if (this.criticalBonusDisplay != null) {
         Config.getInstance().CRITICAL_BONUS_DISPLAY = this.criticalBonusDisplay.getValue();
      }

      if (this.damageBonusDisplay != null) {
         Config.getInstance().DAMAGE_BONUS_DISPLAY = this.damageBonusDisplay.getValue();
      }

      if (this.handBonusDisplay != null) {
         Config.getInstance().HAND_BONUS_DISPLAY = this.handBonusDisplay.getValue();
      }

      if (this.killBonusDisplay != null) {
         Config.getInstance().KILL_BONUS_DISPLAY = this.killBonusDisplay.getValue();
      }

      if (this.longrangeBonusDisplay != null) {
         Config.getInstance().LONGRANGE_BONUS_DISPLAY = this.longrangeBonusDisplay.getValue();
      }

      if (this.magicBonusDisplay != null) {
         Config.getInstance().MAGIC_BONUS_DISPLAY = this.magicBonusDisplay.getValue();
      }

      Config.saveConfig();
      Config.loadRuntimeVariables();
   }

   private void addSpacerAndEntry(ElementConfigScreen.OptionsList.Entry entry) {
      this.list.addEntryPublic(new ElementConfigScreen.OptionsList.SpacerEntry());
      this.list.addEntryPublic(entry);
   }
}
