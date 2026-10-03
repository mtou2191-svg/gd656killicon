package org.mods.gd656killicon.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.mods.gd656killicon.client.ModConfigScreen;

public class ScoreSyncPacket implements CustomPayload {
   public static final CustomPayload.Id<ScoreSyncPacket> ID = new CustomPayload.Id<>(Identifier.of("gd656killicon", "score_sync"));
   public static final PacketCodec<PacketByteBuf, ScoreSyncPacket> CODEC = PacketCodec.of(ScoreSyncPacket::write, ScoreSyncPacket::new);
   private final int score;

   public ScoreSyncPacket(int score) {
      this.score = score;
   }

   public ScoreSyncPacket(PacketByteBuf buf) {
      this.score = buf.readInt();
   }

   void write(PacketByteBuf buf) {
      buf.writeInt(this.score);
   }

   public int getScore() {
      return this.score;
   }

   @Override
   public CustomPayload.Id<? extends CustomPayload> getId() {
      return ID;
   }

   public static void onReceive(ScoreSyncPacket packet, ClientPlayNetworking.Context context) {
      context.client().execute(() -> {
         if (context.client().currentScreen instanceof ModConfigScreen configScreen) {
            configScreen.updateCurrentScore(packet.score);
         }
      });
   }
}
