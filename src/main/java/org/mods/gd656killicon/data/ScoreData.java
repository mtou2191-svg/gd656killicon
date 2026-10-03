package org.mods.gd656killicon.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.scoreboard.ScoreAccess;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

public class ScoreData extends PersistentState {
   private static final String DATA_NAME = "gd656killicon_scores";
   public static final PersistentStateType<ScoreData> TYPE = new PersistentStateType<>(DATA_NAME, ScoreData::new, createCodec(), DataFixTypes.LEVEL);
   private final ConcurrentHashMap<UUID, Integer> playerScores = new ConcurrentHashMap<>();
   private int cardComboKillDurationTime = 60;
   private boolean rankingListEnabled = true;
   private int scoreLimit = 1024;

   public ScoreData() {
   }

   private static Codec<ScoreData> createCodec() {
      Codec<Serialized> serializedCodec = RecordCodecBuilder.create(instance -> instance.group(
         Codec.unboundedMap(Uuids.CODEC, Codec.INT).optionalFieldOf("scores", Map.of()).forGetter(Serialized::scores),
         Codec.BOOL.optionalFieldOf("rankingListEnabled", true).forGetter(Serialized::rankingListEnabled),
         Codec.INT.optionalFieldOf("cardComboKillDurationTime", 60).forGetter(Serialized::cardComboKillDurationTime),
         Codec.INT.optionalFieldOf("scoreLimit", 1024).forGetter(Serialized::scoreLimit)
      ).apply(instance, Serialized::new));
      return serializedCodec.xmap(ScoreData::fromSerialized, ScoreData::toSerialized);
   }

   private record Serialized(Map<UUID, Integer> scores, boolean rankingListEnabled, int cardComboKillDurationTime, int scoreLimit) {
   }

   private static ScoreData fromSerialized(Serialized serialized) {
      ScoreData data = new ScoreData();
      data.playerScores.putAll(serialized.scores());
      data.rankingListEnabled = serialized.rankingListEnabled();
      data.cardComboKillDurationTime = serialized.cardComboKillDurationTime() < 0 ? 60 : serialized.cardComboKillDurationTime();
      data.scoreLimit = serialized.scoreLimit() < 1 ? 1024 : serialized.scoreLimit();
      return data;
   }

   private Serialized toSerialized() {
      return new Serialized(new HashMap<>(this.playerScores), this.rankingListEnabled, this.cardComboKillDurationTime, this.scoreLimit);
   }

   public static ScoreData get(MinecraftServer server) {
      return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE);
   }

   public int getScore(UUID playerId) {
      return this.playerScores.getOrDefault(playerId, 0);
   }

   public void addScore(UUID playerId, float points, MinecraftServer server) {
      int currentScore = this.getScore(playerId);
      if (currentScore >= 1073741823) {
         ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerId);
         if (player != null) {
            player.sendMessage(Text.literal("[六五六] 您的分数已达到上限，无法继续增加").formatted(Formatting.GOLD));
         }
      } else {
         int roundedPoints = Math.round(points);
         if (roundedPoints > this.scoreLimit) {
            roundedPoints = this.scoreLimit;
         }

         int finalPoints = roundedPoints;
         this.playerScores.compute(playerId, (key, current) -> {
            int newScore = (current == null ? 0 : current) + finalPoints;
            if (newScore >= 1073741823) {
               newScore = 1073741823;
               ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerId);
               if (player != null) {
                  player.sendMessage(Text.literal("[六五六] 您的分数已达到上限，无法继续增加").formatted(Formatting.GOLD));
               }
            }

            return newScore;
         });
         this.markDirty();
         this.syncToScoreboard(playerId, server);
      }
   }

   public void reduceScore(UUID playerId, float points, MinecraftServer server) {
      int currentScore = this.getScore(playerId);
      int roundedPoints = Math.round(points);
      int newScore = Math.max(0, currentScore - roundedPoints);
      this.playerScores.put(playerId, newScore);
      this.markDirty();
      this.syncToScoreboard(playerId, server);
   }

   public void setScore(UUID playerId, int score, MinecraftServer server) {
      int newScore = Math.max(0, score);
      this.playerScores.put(playerId, newScore);
      this.markDirty();
      this.syncToScoreboard(playerId, server);
   }

   public Map<UUID, Integer> getAllScores() {
      return new HashMap<>(this.playerScores);
   }

   public boolean getRankingListEnabled() {
      return this.rankingListEnabled;
   }

   public void setRankingListEnabled(boolean enabled) {
      this.rankingListEnabled = enabled;
      this.markDirty();
   }

   public int getCardComboKillDurationTime() {
      return this.cardComboKillDurationTime;
   }

   public void setCardComboKillDurationTime(int duration) {
      this.cardComboKillDurationTime = Math.max(0, Math.min(duration, 3600));
      this.markDirty();
   }

   public void setScoreLimit(int limit) {
      this.scoreLimit = Math.max(1, Math.min(limit, 2147483646));
      this.markDirty();
   }

   private void syncToScoreboard(UUID playerId, MinecraftServer server) {
      ScoreboardBindingData bindingData = ScoreboardBindingData.get(server);
      if (bindingData.isBound()) {
         Scoreboard scoreboard = server.getScoreboard();
         ScoreboardObjective objective = scoreboard.getNullableObjective(bindingData.getObjectiveName());
         if (objective != null) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerId);
            if (player != null) {
               int score = this.getScore(playerId);
               ScoreAccess scoreAccess = scoreboard.getOrCreateScore(player, objective);
               scoreAccess.setScore(score);
            }
         }
      }
   }
}
