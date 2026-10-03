package org.mods.gd656killicon;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AfterDeath;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AllowDamage;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.mods.gd656killicon.commands.ScoreCommand;
import org.mods.gd656killicon.data.BanListData;
import org.mods.gd656killicon.data.ScoreData;
import org.mods.gd656killicon.data.ScoreExpressionManager;
import org.mods.gd656killicon.data.ScoreboardBindingData;
import org.mods.gd656killicon.events.PlayerEventHandler;
import org.mods.gd656killicon.network.DamageScorePacket;
import org.mods.gd656killicon.network.KillPacket;
import org.mods.gd656killicon.network.NetworkHandler;
import org.mods.gd656killicon.network.ScoreSyncPacket;
import org.mods.gd656killicon.util.ScoreExpressionCalculator;

public class Gd656killicon implements ModInitializer {
   public static final String MOD_ID = "gd656killicon";
   public static final SoundEvent ASSIST_KILL_VANILLA = SoundEvent.of(Identifier.of("gd656killicon", "assistkill_sound_vanilla"));
   public static final SoundEvent CARD_KILL_VANILLA = SoundEvent.of(Identifier.of("gd656killicon", "cardkill_sound_vanilla"));
   public static final SoundEvent COMBO_1_VANILLA = SoundEvent.of(Identifier.of("gd656killicon", "combo_1_vanilla"));
   public static final SoundEvent COMBO_2_VANILLA = SoundEvent.of(Identifier.of("gd656killicon", "combo_2_vanilla"));
   public static final SoundEvent COMBO_3_VANILLA = SoundEvent.of(Identifier.of("gd656killicon", "combo_3_vanilla"));
   public static final SoundEvent COMBO_4_VANILLA = SoundEvent.of(Identifier.of("gd656killicon", "combo_4_vanilla"));
   public static final SoundEvent COMBO_5_VANILLA = SoundEvent.of(Identifier.of("gd656killicon", "combo_5_vanilla"));
   public static final SoundEvent COMBO_6_VANILLA = SoundEvent.of(Identifier.of("gd656killicon", "combo_6_vanilla"));
   public static final SoundEvent KILL_SOUND_VANILLA = SoundEvent.of(Identifier.of("gd656killicon", "kill_sound_vanilla"));
   public static final SoundEvent ULTIMATE_KILL_VANILLA = SoundEvent.of(Identifier.of("gd656killicon", "ultimatekill_sound_vanilla"));
   public static final SoundEvent ASSIST_KILL_MODERN = SoundEvent.of(Identifier.of("gd656killicon", "assistkill_sound_modern"));
   public static final SoundEvent CARD_KILL_MODERN = SoundEvent.of(Identifier.of("gd656killicon", "cardkill_sound_modern"));
   public static final SoundEvent COMBO_1_MODERN = SoundEvent.of(Identifier.of("gd656killicon", "combo_1_modern"));
   public static final SoundEvent COMBO_2_MODERN = SoundEvent.of(Identifier.of("gd656killicon", "combo_2_modern"));
   public static final SoundEvent COMBO_3_MODERN = SoundEvent.of(Identifier.of("gd656killicon", "combo_3_modern"));
   public static final SoundEvent COMBO_4_MODERN = SoundEvent.of(Identifier.of("gd656killicon", "combo_4_modern"));
   public static final SoundEvent COMBO_5_MODERN = SoundEvent.of(Identifier.of("gd656killicon", "combo_5_modern"));
   public static final SoundEvent COMBO_6_MODERN = SoundEvent.of(Identifier.of("gd656killicon", "combo_6_modern"));
   public static final SoundEvent KILL_SOUND_MODERN = SoundEvent.of(Identifier.of("gd656killicon", "kill_sound_modern"));
   public static final SoundEvent ULTIMATE_KILL_MODERN = SoundEvent.of(Identifier.of("gd656killicon", "ultimatekill_sound_modern"));
   private static final Map<UUID, Map<UUID, AssistData>> assistDataMap = new HashMap<>();
   private static final Map<UUID, Long> lastDamageTimeMap = new HashMap<>();
   private static final Map<UUID, Boolean> playerCritMap = new HashMap<>();
   private static final Map<UUID, Integer> playerComboCount = new HashMap<>();
   private static final int SOUND_COOLDOWN_MS = 100;
   private static final long ASSIST_TIMEOUT = 60000L;

   public void onInitialize() {
      Config.initialize();
      NetworkHandler.registerCommon();
      NetworkHandler.registerServerReceivers();
      ServerLivingEntityEvents.ALLOW_DAMAGE.register((AllowDamage)(entity, source, amount) -> {
         this.onLivingDamage(entity, source, amount);
         return true;
      });
      ServerLivingEntityEvents.AFTER_DEATH.register((AfterDeath)(entity, source) -> this.onLivingDeath(entity, source));
      CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> ScoreCommand.register(dispatcher));
      ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarting);
      PlayerEventHandler.registerPlayerEvents();
      this.registerSoundEvents();
   }

   private void registerSoundEvents() {
      Registry.register(Registries.SOUND_EVENT, ASSIST_KILL_VANILLA.id(), ASSIST_KILL_VANILLA);
      Registry.register(Registries.SOUND_EVENT, CARD_KILL_VANILLA.id(), CARD_KILL_VANILLA);
      Registry.register(Registries.SOUND_EVENT, COMBO_1_VANILLA.id(), COMBO_1_VANILLA);
      Registry.register(Registries.SOUND_EVENT, COMBO_2_VANILLA.id(), COMBO_2_VANILLA);
      Registry.register(Registries.SOUND_EVENT, COMBO_3_VANILLA.id(), COMBO_3_VANILLA);
      Registry.register(Registries.SOUND_EVENT, COMBO_4_VANILLA.id(), COMBO_4_VANILLA);
      Registry.register(Registries.SOUND_EVENT, COMBO_5_VANILLA.id(), COMBO_5_VANILLA);
      Registry.register(Registries.SOUND_EVENT, COMBO_6_VANILLA.id(), COMBO_6_VANILLA);
      Registry.register(Registries.SOUND_EVENT, KILL_SOUND_VANILLA.id(), KILL_SOUND_VANILLA);
      Registry.register(Registries.SOUND_EVENT, ULTIMATE_KILL_VANILLA.id(), ULTIMATE_KILL_VANILLA);
      Registry.register(Registries.SOUND_EVENT, ASSIST_KILL_MODERN.id(), ASSIST_KILL_MODERN);
      Registry.register(Registries.SOUND_EVENT, CARD_KILL_MODERN.id(), CARD_KILL_MODERN);
      Registry.register(Registries.SOUND_EVENT, COMBO_1_MODERN.id(), COMBO_1_MODERN);
      Registry.register(Registries.SOUND_EVENT, COMBO_2_MODERN.id(), COMBO_2_MODERN);
      Registry.register(Registries.SOUND_EVENT, COMBO_3_MODERN.id(), COMBO_3_MODERN);
      Registry.register(Registries.SOUND_EVENT, COMBO_4_MODERN.id(), COMBO_4_MODERN);
      Registry.register(Registries.SOUND_EVENT, COMBO_5_MODERN.id(), COMBO_5_MODERN);
      Registry.register(Registries.SOUND_EVENT, COMBO_6_MODERN.id(), COMBO_6_MODERN);
      Registry.register(Registries.SOUND_EVENT, KILL_SOUND_MODERN.id(), KILL_SOUND_MODERN);
      Registry.register(Registries.SOUND_EVENT, ULTIMATE_KILL_MODERN.id(), ULTIMATE_KILL_MODERN);
   }

   private void onLivingDamage(LivingEntity entity, DamageSource source, float amount) {
      if (!entity.getEntityWorld().isClient()) {
         Identifier entityId = Registries.ENTITY_TYPE.getId(entity.getType());
         boolean isBanned = BanListData.get(entity.getEntityWorld().getServer()).isBanned(entityId.toString());
         if (source.getAttacker() instanceof ServerPlayerEntity player) {
            byte damageType = this.getDamageType(player, source);
            String reason = this.getDamageReason(damageType);
            if (!isBanned) {
               ServerPlayNetworking.send(player, new DamageScorePacket(amount, reason));
            }

            lastDamageTimeMap.put(player.getUuid(), System.currentTimeMillis());
            playerCritMap.put(player.getUuid(), this.isPlayerCritical(player));
            if (!isBanned) {
               this.handleDamageScore(player, entity, amount, damageType);
            }

            this.handleAssistData(entity, player, amount);
            this.cleanupExpiredAssistData();
         }
      }
   }

   private void onLivingDeath(LivingEntity entity, DamageSource source) {
      if (!entity.getEntityWorld().isClient()) {
         Identifier entityId = Registries.ENTITY_TYPE.getId(entity.getType());
         boolean isBanned = BanListData.get(entity.getEntityWorld().getServer()).isBanned(entityId.toString());
         MinecraftServer server = entity.getEntityWorld().getServer();
         if (server != null) {
            if (source.getAttacker() instanceof ServerPlayerEntity killer) {
               this.handleKillEvent(killer, entity, source, isBanned);
            }

            this.handleAssistKills(entity, source, server, isBanned);
         }
      }
   }

   private void onServerStarting(MinecraftServer server) {
      ScoreData.get(server);
      BanListData.get(server);
      ScoreExpressionManager.get(server);
      ScoreboardBindingData.get(server);
      this.startScoreboardSyncTask(server);
   }

   private void handleDamageScore(ServerPlayerEntity player, LivingEntity target, float damageAmount, byte damageType) {
      ScoreData scoreData = ScoreData.get(player.getEntityWorld().getServer());
      ScoreExpressionManager expressionManager = ScoreExpressionManager.get(player.getEntityWorld().getServer());
      Map<String, Double> variables = new HashMap<>();
      variables.put("damage", (double)damageAmount);
      variables.put("health", (double)target.getMaxHealth());
      String expressionKey = this.getDamageExpressionKey(damageType);
      double calculatedScore = ScoreExpressionCalculator.calculate(expressionManager.getExpression(expressionKey), variables);
      scoreData.addScore(player.getUuid(), (float)calculatedScore, player.getEntityWorld().getServer());
      ServerPlayNetworking.send(player, new ScoreSyncPacket(scoreData.getScore(player.getUuid())));
   }

   private void handleKillEvent(ServerPlayerEntity killer, LivingEntity target, DamageSource source, boolean isBanned) {
      ScoreExpressionManager expressionManager = ScoreExpressionManager.get(killer.getEntityWorld().getServer());
      String targetKey = this.getTargetName(target);
      boolean isCritical = playerCritMap.getOrDefault(killer.getUuid(), false);
      Map<String, Double> variables = new HashMap<>();
      variables.put("health", (double)target.getMaxHealth());
      double killScore = ScoreExpressionCalculator.calculate(expressionManager.getExpression("kill"), variables);
      if (isCritical) {
         variables.put("killscore", killScore);
         double critBonus = ScoreExpressionCalculator.calculate(expressionManager.getExpression("critical"), variables);
         killScore += critBonus;
      }

      byte damageType = this.getDamageType(killer, source);
      int displayScore = isBanned ? 0 : (int)Math.round(killScore);
      ServerPlayNetworking.send(killer, new KillPacket(targetKey, isCritical, isCritical, displayScore, false, damageType));
      if (!isBanned) {
         ScoreData scoreData = ScoreData.get(killer.getEntityWorld().getServer());
         scoreData.addScore(killer.getUuid(), (float)killScore, killer.getEntityWorld().getServer());
         ServerPlayNetworking.send(killer, new ScoreSyncPacket(scoreData.getScore(killer.getUuid())));
         this.updateComboCount(killer.getUuid());
      }

      this.handleLongRangeKill(killer, target, isBanned);
      playerCritMap.remove(killer.getUuid());
   }

   private void handleAssistKills(LivingEntity target, DamageSource source, MinecraftServer server, boolean isBanned) {
      UUID targetId = target.getUuid();
      if (assistDataMap.containsKey(targetId)) {
         Map<UUID, AssistData> targetAssists = assistDataMap.get(targetId);
         float maxHealth = target.getMaxHealth();
         UUID killerId = source.getAttacker() instanceof ServerPlayerEntity killer ? killer.getUuid() : null;

         for (AssistData assist : targetAssists.values()) {
            if (!assist.attackerId.equals(killerId)) {
               ServerPlayerEntity assister = server.getPlayerManager().getPlayer(assist.attackerId);
               if (assister != null) {
                  ScoreExpressionManager expressionManager = ScoreExpressionManager.get(server);
                  Map<String, Double> variables = new HashMap<>();
                  variables.put("damagedealt", (double)assist.damageDealt);
                  variables.put("health", (double)maxHealth);
                  double assistScore = ScoreExpressionCalculator.calculate(expressionManager.getExpression("assist"), variables);
                  if (isBanned) {
                     assistScore = 0.0;
                  }

                  byte damageType = this.getDamageType(assister, source);
                  ServerPlayNetworking.send(
                     assister, new KillPacket(this.getTargetName(target), false, false, (int)Math.round(assistScore), true, damageType)
                  );
                  if (!isBanned) {
                     ScoreData scoreData = ScoreData.get(server);
                     scoreData.addScore(assister.getUuid(), (float)assistScore, server);
                     ServerPlayNetworking.send(assister, new ScoreSyncPacket(scoreData.getScore(assister.getUuid())));
                     this.updateComboCount(assister.getUuid());
                  }
               }
            }
         }

         assistDataMap.remove(targetId);
      }
   }

   private void handleLongRangeKill(ServerPlayerEntity killer, LivingEntity target, boolean isBanned) {
      double distance = killer.distanceTo(target);
      if (distance >= 20.0) {
         ScoreExpressionManager expressionManager = ScoreExpressionManager.get(killer.getEntityWorld().getServer());
         Map<String, Double> variables = new HashMap<>();
         variables.put("distance", distance);
         double distanceScore = ScoreExpressionCalculator.calculate(expressionManager.getExpression("longrange"), variables);
         if (!isBanned) {
            String reason = "远距离击败 " + (int)distance + "m";
            ServerPlayNetworking.send(killer, new DamageScorePacket((float)distanceScore, reason));
            ScoreData scoreData = ScoreData.get(killer.getEntityWorld().getServer());
            scoreData.addScore(killer.getUuid(), (float)distanceScore, killer.getEntityWorld().getServer());
            ServerPlayNetworking.send(killer, new ScoreSyncPacket(scoreData.getScore(killer.getUuid())));
         }
      }
   }

   private void handleAssistData(LivingEntity target, ServerPlayerEntity player, float damageAmount) {
      UUID targetId = target.getUuid();
      UUID attackerId = player.getUuid();
      if (!assistDataMap.containsKey(targetId)) {
         assistDataMap.put(targetId, new HashMap<>());
      }

      Map<UUID, AssistData> targetAssists = assistDataMap.get(targetId);
      if (targetAssists.containsKey(attackerId)) {
         targetAssists.computeIfPresent(attackerId, (k, existing) -> new AssistData(attackerId, existing.damageDealt + damageAmount));
      } else {
         targetAssists.put(attackerId, new AssistData(attackerId, damageAmount));
      }
   }

   private void cleanupExpiredAssistData() {
      long currentTime = System.currentTimeMillis();
      assistDataMap.values().removeIf(targetAssists -> {
         targetAssists.entrySet().removeIf(e -> currentTime - e.getValue().timestamp > 60000L);
         return targetAssists.isEmpty();
      });
   }

   private void startScoreboardSyncTask(MinecraftServer server) {
      Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
         if (server.isRunning()) {
            server.execute(() -> ScoreCommand.syncScoresToScoreboard(server));
         }
      }, 1L, 1L, TimeUnit.SECONDS);
   }

   private void updateComboCount(UUID playerId) {
      long currentTime = System.currentTimeMillis();
      int comboCount = playerComboCount.getOrDefault(playerId, 0);
      if (currentTime - lastDamageTimeMap.getOrDefault(playerId, 0L) > Config.comboTimeoutMs) {
         comboCount = 0;
      }

      playerComboCount.put(playerId, ++comboCount);
      lastDamageTimeMap.put(playerId, currentTime);
   }

   private byte getDamageType(ServerPlayerEntity player, DamageSource source) {
      if (this.isMagicDamage(source)) {
         return 1;
      } else {
         return (byte)(player.getMainHandStack().isEmpty() ? 2 : 0);
      }
   }

   private String getDamageReason(byte damageType) {
      return switch (damageType) {
         case 1 -> "魔法伤害";
         case 2 -> "空手攻击";
         default -> "造成伤害";
      };
   }

   private String getDamageExpressionKey(byte damageType) {
      return switch (damageType) {
         case 1 -> "magic";
         case 2 -> "hand";
         default -> "damage";
      };
   }

   private boolean isPlayerCritical(ServerPlayerEntity player) {
      return player.fallDistance > 0.0F
         && !player.isOnGround()
         && !player.isGliding()
         && !player.isTouchingWater()
         && !player.hasStatusEffect(StatusEffects.BLINDNESS)
         && !player.hasVehicle()
         && !player.isInSneakingPose();
   }

   private boolean isMagicDamage(DamageSource source) {
      String msgId = source.getName();
      return msgId.contains("magic")
         || msgId.contains("potion")
         || msgId.contains("indirect")
         || msgId.contains("thrown")
         || msgId.contains("wither")
         || msgId.contains("dragon");
   }

   private String getTargetName(LivingEntity target) {
      return target instanceof ServerPlayerEntity ? target.getName().getString() : target.getType().getName().getString();
   }

   public static SoundEvent getCardKillSound() {
      return Config.iconStyle == Config.IconStyle.VANILLA ? CARD_KILL_VANILLA : CARD_KILL_MODERN;
   }

   static class AssistData {
      public final UUID attackerId;
      public final float damageDealt;
      public final long timestamp;

      public AssistData(UUID attackerId, float damageDealt) {
         this.attackerId = attackerId;
         this.damageDealt = damageDealt;
         this.timestamp = System.currentTimeMillis();
      }
   }
}
