package org.mods.gd656killicon.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.mods.gd656killicon.data.ScoreData;

public class ScoreRequestPacket implements CustomPayload {
   public static final CustomPayload.Id<ScoreRequestPacket> ID = new CustomPayload.Id<>(Identifier.of("gd656killicon", "score_request"));
   public static final ScoreRequestPacket INSTANCE = new ScoreRequestPacket();
   public static final PacketCodec<PacketByteBuf, ScoreRequestPacket> CODEC = PacketCodec.unit(INSTANCE);

   @Override
   public CustomPayload.Id<? extends CustomPayload> getId() {
      return ID;
   }

   public static void onReceive(ScoreRequestPacket packet, ServerPlayNetworking.Context context) {
      context.server().execute(() -> {
         ScoreData scoreData = ScoreData.get(context.server());
         context.responseSender().sendPacket(new ScoreSyncPacket(scoreData.getScore(context.player().getUuid())));
      });
   }
}
