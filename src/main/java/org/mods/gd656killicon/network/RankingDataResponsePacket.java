package org.mods.gd656killicon.network;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.mods.gd656killicon.client.configmenu.RankingListScreen;

public class RankingDataResponsePacket implements CustomPayload {
   public static final CustomPayload.Id<RankingDataResponsePacket> ID = new CustomPayload.Id<>(Identifier.of("gd656killicon", "ranking_data_response"));
   public static final PacketCodec<PacketByteBuf, RankingDataResponsePacket> CODEC = PacketCodec.of(
      RankingDataResponsePacket::write, RankingDataResponsePacket::new
   );
   private final Map<UUID, Integer> rankingData;
   private final boolean rankingEnabled;

   public RankingDataResponsePacket(Map<UUID, Integer> rankingData, boolean rankingEnabled) {
      this.rankingData = rankingData;
      this.rankingEnabled = rankingEnabled;
   }

   public RankingDataResponsePacket(PacketByteBuf buf) {
      this.rankingEnabled = buf.readBoolean();
      int size = buf.readInt();
      this.rankingData = new HashMap<>();

      for (int i = 0; i < size; i++) {
         this.rankingData.put(buf.readUuid(), buf.readInt());
      }
   }

   void write(PacketByteBuf buf) {
      buf.writeBoolean(this.rankingEnabled);
      buf.writeInt(this.rankingData.size());
      this.rankingData.forEach((id, score) -> {
         buf.writeUuid(id);
         buf.writeInt(score);
      });
   }

   @Override
   public CustomPayload.Id<? extends CustomPayload> getId() {
      return ID;
   }

   public static void onReceive(RankingDataResponsePacket packet, ClientPlayNetworking.Context context) {
      context.client().execute(() -> {
         RankingListScreen instance = RankingListScreen.getInstance();
         if (instance != null) {
            instance.updateRankingData(packet.rankingData, packet.rankingEnabled);
         }
      });
   }
}
