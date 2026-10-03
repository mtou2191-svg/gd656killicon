package org.mods.gd656killicon.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.mods.gd656killicon.client.configmenu.AboutScreen;
import org.mods.gd656killicon.client.configmenu.FeedbackScreen;
import org.mods.gd656killicon.client.configmenu.HelpScreen;
import org.mods.gd656killicon.client.configmenu.HistoryRecordScreen;
import org.mods.gd656killicon.client.configmenu.RankingListScreen;

public class MenuManager {
   private static MenuManager instance;
   public static final String MOD_VERSION = "1.0.0 公测版 RC5 *Fabric*";
   public static final String MINECRAFT_VERSION = "1.20.1";
   public static final String DEVELOPER = "Minecraft_GD656";

   private MenuManager() {
   }

   public static MenuManager getInstance() {
      if (instance == null) {
         instance = new MenuManager();
      }

      return instance;
   }

   public void openRankingList(Screen parent) {
      MinecraftClient.getInstance().setScreen(new RankingListScreen(parent));
   }

   public void openHistoryRecord(Screen parent) {
      MinecraftClient.getInstance().setScreen(new HistoryRecordScreen(parent));
   }

   public void openMoreSettings(Screen parent) {
      MinecraftClient.getInstance().setScreen(new ConfigScreen(parent));
   }

   public void openHelp(Screen parent) {
      MinecraftClient.getInstance().setScreen(new HelpScreen(parent));
   }

   public void openAbout(Screen parent) {
      MinecraftClient.getInstance().setScreen(new AboutScreen(parent));
   }

   public void openFeedback(Screen parent) {
      MinecraftClient.getInstance().setScreen(new FeedbackScreen(parent));
   }
}
