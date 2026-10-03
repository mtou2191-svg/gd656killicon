package org.mods.gd656killicon.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.mods.gd656killicon.client.KillIconRenderer;

public class KillPacket implements CustomPayload {
   public static final CustomPayload.Id<KillPacket> ID = new CustomPayload.Id<>(Identifier.of("gd656killicon", "kill"));
   public static final PacketCodec<PacketByteBuf, KillPacket> CODEC = PacketCodec.of(KillPacket::write, KillPacket::new);
   private final String targetName;
   private final boolean isCritical;
   private final boolean playUltimateSound;
   private final int killScore;
   private final boolean isAssist;
   private final byte damageType;

   public KillPacket(String targetName, boolean isCritical, boolean playUltimateSound, int killScore, boolean isAssist, byte damageType) {
      this.targetName = targetName;
      this.isCritical = isCritical;
      this.playUltimateSound = playUltimateSound;
      this.killScore = killScore;
      this.isAssist = isAssist;
      this.damageType = damageType;
   }

   public KillPacket(PacketByteBuf buf) {
      this.targetName = buf.readString();
      this.isCritical = buf.readBoolean();
      this.playUltimateSound = buf.readBoolean();
      this.killScore = buf.readInt();
      this.isAssist = buf.readBoolean();
      this.damageType = buf.readByte();
   }

   void write(PacketByteBuf buf) {
      buf.writeString(this.targetName);
      buf.writeBoolean(this.isCritical);
      buf.writeBoolean(this.playUltimateSound);
      buf.writeInt(this.killScore);
      buf.writeBoolean(this.isAssist);
      buf.writeByte(this.damageType);
   }

   @Override
   public CustomPayload.Id<? extends CustomPayload> getId() {
      return ID;
   }

   public static void onReceive(KillPacket packet, ClientPlayNetworking.Context context) {
      context.client().execute(() -> {
         MinecraftClient mc = context.client();
         String weaponName = "拳头";
         if (packet.damageType == 1) {
            weaponName = "魔法伤害";
         } else if (packet.damageType == 0 && mc.player != null && !mc.player.getMainHandStack().isEmpty()) {
            weaponName = mc.player.getMainHandStack().getItem().getName().getString().replace("[", "").replace("]", "");
         }

         KillIconRenderer.triggerKillIcon(weaponName, packet.targetName, packet.isCritical, packet.playUltimateSound, packet.killScore, packet.isAssist);
      });
   }
}
