package org.mods.gd656killicon.network;

import java.util.HashMap;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.mods.gd656killicon.data.ScoreData;

public class RankingDataRequestPacket implements CustomPayload {
   public static final CustomPayload.Id<RankingDataRequestPacket> ID = new CustomPayload.Id<>(
      Identifier.of("gd656killicon", "ranking_data_request")
   );
   public static final RankingDataRequestPacket INSTANCE = new RankingDataRequestPacket();
   public static final PacketCodec<PacketByteBuf, RankingDataRequestPacket> CODEC = PacketCodec.unit(INSTANCE);

   @Override
   public CustomPayload.Id<? extends CustomPayload> getId() {
      return ID;
   }

   public static void onReceive(RankingDataRequestPacket packet, ServerPlayNetworking.Context context) {
      context.server().execute(() -> {
         ScoreData scoreData = ScoreData.get(context.server());
         RankingDataResponsePacket response = scoreData.getRankingListEnabled()
            ? new RankingDataResponsePacket(scoreData.getAllScores(), true)
            : new RankingDataResponsePacket(new HashMap<>(), false);
         context.responseSender().sendPacket(response);
      });
   }
}
