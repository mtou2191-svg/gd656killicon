package org.mods.gd656killicon.events;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import org.mods.gd656killicon.client.KeyBindings;
import org.mods.gd656killicon.client.ModConfigScreen;
import org.mods.gd656killicon.client.configmenu.HistoryRecordScreen;
import org.mods.gd656killicon.client.configmenu.RankingListScreen;

public class ClientEventHandler {
   public static void registerKeyMappings() {
      KeyBindingHelper.registerKeyBinding(KeyBindings.OPEN_CONFIG);
      KeyBindingHelper.registerKeyBinding(KeyBindings.OPEN_HISTORY);
      KeyBindingHelper.registerKeyBinding(KeyBindings.OPEN_RANKING);
   }

   public static void registerClientTick() {
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> {
         if (client.player != null && client.currentScreen == null) {
            if (KeyBindings.OPEN_CONFIG.wasPressed()) {
               client.setScreen(new ModConfigScreen(null));
            } else if (KeyBindings.OPEN_RANKING.wasPressed()) {
               client.setScreen(new RankingListScreen(null));
            } else if (KeyBindings.OPEN_HISTORY.wasPressed()) {
               client.setScreen(new HistoryRecordScreen(null));
            }
         }
      });
   }
}
