package org.mods.gd656killicon.events;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.Join;
import org.mods.gd656killicon.data.ScoreData;
import org.mods.gd656killicon.network.CardComboKillDurationPacket;

public class PlayerEventHandler {
   public static void registerPlayerEvents() {
      ServerPlayConnectionEvents.JOIN.register((Join)(handler, sender, server) -> {
         ScoreData scoreData = ScoreData.get(server);
         sender.sendPacket(new CardComboKillDurationPacket(scoreData.getCardComboKillDurationTime()));
      });
   }
}
