package org.mods.gd656killicon.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.mods.gd656killicon.client.iconrenderer.CardModeRenderer;

public class CardComboKillDurationPacket implements CustomPayload {
   public static final CustomPayload.Id<CardComboKillDurationPacket> ID = new CustomPayload.Id<>(
      Identifier.of("gd656killicon", "card_combo_kill_duration")
   );
   public static final PacketCodec<PacketByteBuf, CardComboKillDurationPacket> CODEC = PacketCodec.of(
      CardComboKillDurationPacket::write, CardComboKillDurationPacket::new
   );
   private final int duration;

   public CardComboKillDurationPacket(int duration) {
      this.duration = duration;
   }

   public CardComboKillDurationPacket(PacketByteBuf buf) {
      this.duration = buf.readInt();
   }

   void write(PacketByteBuf buf) {
      buf.writeInt(this.duration);
   }

   public int getDuration() {
      return this.duration;
   }

   @Override
   public CustomPayload.Id<? extends CustomPayload> getId() {
      return ID;
   }

   public static void onReceive(CardComboKillDurationPacket packet, ClientPlayNetworking.Context context) {
      context.client().execute(() -> CardModeRenderer.setKillChainTimeout(packet.duration * 1000));
   }
}
