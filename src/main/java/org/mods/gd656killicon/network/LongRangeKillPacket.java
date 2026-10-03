package org.mods.gd656killicon.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.mods.gd656killicon.client.KillIconRenderer;

public class LongRangeKillPacket implements CustomPayload {
   public static final CustomPayload.Id<LongRangeKillPacket> ID = new CustomPayload.Id<>(Identifier.of("gd656killicon", "long_range_kill"));
   public static final PacketCodec<PacketByteBuf, LongRangeKillPacket> CODEC = PacketCodec.of(LongRangeKillPacket::write, LongRangeKillPacket::new);
   private final int distance;
   private final int bonusPoints;

   public LongRangeKillPacket(int distance, int bonusPoints) {
      this.distance = distance;
      this.bonusPoints = bonusPoints;
   }

   public LongRangeKillPacket(PacketByteBuf buf) {
      this.distance = buf.readInt();
      this.bonusPoints = buf.readInt();
   }

   void write(PacketByteBuf buf) {
      buf.writeInt(this.distance);
      buf.writeInt(this.bonusPoints);
   }

   @Override
   public CustomPayload.Id<? extends CustomPayload> getId() {
      return ID;
   }

   public static void onReceive(LongRangeKillPacket packet, ClientPlayNetworking.Context context) {
      context.client().execute(() -> KillIconRenderer.showLongRangeBonus(packet.distance, packet.bonusPoints));
   }
}
