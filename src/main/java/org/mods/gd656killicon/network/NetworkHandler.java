package org.mods.gd656killicon.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class NetworkHandler {
   public static void registerCommon() {
      PayloadTypeRegistry.playS2C().register(KillPacket.ID, KillPacket.CODEC);
      PayloadTypeRegistry.playS2C().register(DamageScorePacket.ID, DamageScorePacket.CODEC);
      PayloadTypeRegistry.playS2C().register(ScoreSyncPacket.ID, ScoreSyncPacket.CODEC);
      PayloadTypeRegistry.playS2C().register(LongRangeKillPacket.ID, LongRangeKillPacket.CODEC);
      PayloadTypeRegistry.playS2C().register(RankingDataResponsePacket.ID, RankingDataResponsePacket.CODEC);
      PayloadTypeRegistry.playS2C().register(CardComboKillDurationPacket.ID, CardComboKillDurationPacket.CODEC);
      PayloadTypeRegistry.playC2S().register(ScoreRequestPacket.ID, ScoreRequestPacket.CODEC);
      PayloadTypeRegistry.playC2S().register(RankingDataRequestPacket.ID, RankingDataRequestPacket.CODEC);
   }

   public static void registerServerReceivers() {
      ServerPlayNetworking.registerGlobalReceiver(ScoreRequestPacket.ID, ScoreRequestPacket::onReceive);
      ServerPlayNetworking.registerGlobalReceiver(RankingDataRequestPacket.ID, RankingDataRequestPacket::onReceive);
   }

   public static void registerClientReceivers() {
      ClientPlayNetworking.registerGlobalReceiver(KillPacket.ID, KillPacket::onReceive);
      ClientPlayNetworking.registerGlobalReceiver(DamageScorePacket.ID, DamageScorePacket::onReceive);
      ClientPlayNetworking.registerGlobalReceiver(ScoreSyncPacket.ID, ScoreSyncPacket::onReceive);
      ClientPlayNetworking.registerGlobalReceiver(LongRangeKillPacket.ID, LongRangeKillPacket::onReceive);
      ClientPlayNetworking.registerGlobalReceiver(RankingDataResponsePacket.ID, RankingDataResponsePacket::onReceive);
      ClientPlayNetworking.registerGlobalReceiver(CardComboKillDurationPacket.ID, CardComboKillDurationPacket::onReceive);
   }

   public static void sendToClient(ServerPlayerEntity player, Object packet) {
      if (packet instanceof ScoreSyncPacket scoreSyncPacket) {
         ServerPlayNetworking.send(player, scoreSyncPacket);
      } else if (packet instanceof RankingDataResponsePacket rankingDataResponsePacket) {
         ServerPlayNetworking.send(player, rankingDataResponsePacket);
      } else if (packet instanceof CardComboKillDurationPacket cardComboPacket) {
         ServerPlayNetworking.send(player, cardComboPacket);
      }
   }

   public static void sendToAllPlayers(Object packet, MinecraftServer server) {
      for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
         sendToClient(player, packet);
      }
   }
}
