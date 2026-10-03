package org.mods.gd656killicon.client;

import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.entity.effect.StatusEffects;
import org.mods.gd656killicon.network.ScoreRequestPacket;

public class ClientKillTracker {
   private static final long ATTACK_WINDOW_MS = 5000L;
   private static final double PROJECTILE_RANGE = 4.0;
   private static final Map<Integer, AttackRecord> attacks = new HashMap<>();

   public static void recordAttack(Entity target, PlayerEntity player) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.world == null || mc.player == null || !(target instanceof LivingEntity)) {
         return;
      }

      boolean crit = player.fallDistance > 0.0F
         && !player.isOnGround()
         && !player.isGliding()
         && !player.isTouchingWater()
         && !player.hasVehicle()
         && !player.isInSneakingPose()
         && !player.hasStatusEffect(StatusEffects.BLINDNESS);
      attacks.put(target.getId(), new AttackRecord(System.currentTimeMillis(), weaponName(player), crit));
   }

   public static void onEntityStatus(EntityStatusS2CPacket packet) {
      if (packet.getStatus() != 3) {
         return;
      }

      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.world == null || mc.player == null) {
         return;
      }

      if (ClientPlayNetworking.canSend(ScoreRequestPacket.ID)) {
         return;
      }

      Entity entity = packet.getEntity(mc.world);
      if (!(entity instanceof LivingEntity) || entity == mc.player) {
         return;
      }

      long now = System.currentTimeMillis();
      AttackRecord record = attacks.get(entity.getId());
      boolean melee = record != null && now - record.time <= ATTACK_WINDOW_MS;
      boolean ranged = false;
      if (!melee) {
         for (Entity other : mc.world.getEntities()) {
            if (other instanceof PersistentProjectileEntity projectile && projectile.getOwner() == mc.player && projectile.distanceTo(entity) <= PROJECTILE_RANGE) {
               ranged = true;
               break;
            }
         }
      }

      if (!melee && !ranged) {
         return;
      }

      String weapon = melee ? record.weapon : weaponName(mc.player);
      boolean crit = melee && record.crit;
      String targetName = entity instanceof PlayerEntity player ? player.getName().getString() : entity.getType().getName().getString();
      KillIconRenderer.triggerKillIcon(weapon, targetName, crit, crit, 0, false);
   }

   public static void clear() {
      attacks.clear();
   }

   private static String weaponName(PlayerEntity player) {
      ItemStack stack = player.getMainHandStack();
      return stack.isEmpty() ? "拳头" : stack.getItem().getName().getString().replace("[", "").replace("]", "");
   }

   private static class AttackRecord {
      private final long time;
      private final String weapon;
      private final boolean crit;

      private AttackRecord(long time, String weapon, boolean crit) {
         this.time = time;
         this.weapon = weapon;
         this.crit = crit;
      }
   }
}
