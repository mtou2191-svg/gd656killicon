package org.mods.gd656killicon.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.mods.gd656killicon.client.KillIconRenderer;
import org.mods.gd656killicon.client.configmenu.HistoryRecordScreen;

public class DamageScorePacket implements CustomPayload {
   public static final CustomPayload.Id<DamageScorePacket> ID = new CustomPayload.Id<>(Identifier.of("gd656killicon", "damage_score"));
   public static final PacketCodec<PacketByteBuf, DamageScorePacket> CODEC = PacketCodec.of(DamageScorePacket::write, DamageScorePacket::new);
   private final float damage;
   private final String reason;

   public DamageScorePacket(float damage, String reason) {
      this.damage = damage;
      this.reason = reason;
   }

   public DamageScorePacket(PacketByteBuf buf) {
      this.damage = buf.readFloat();
      this.reason = buf.readString();
   }

   void write(PacketByteBuf buf) {
      buf.writeFloat(this.damage);
      buf.writeString(this.reason);
   }

   @Override
   public CustomPayload.Id<? extends CustomPayload> getId() {
      return ID;
   }

   public static void onReceive(DamageScorePacket packet, ClientPlayNetworking.Context context) {
      context.client().execute(() -> {
         KillIconRenderer.addScore(packet.damage, packet.reason, false);
         if (packet.reason.contains("助攻") || packet.reason.contains("assist")) {
            HistoryRecordScreen.addHistoryRecord("未知目标", "助攻 +" + Math.round(packet.damage), "未知武器", false, false);
         }
      });
   }
}
