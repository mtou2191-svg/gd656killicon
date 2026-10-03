package org.mods.gd656killicon;

import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.mods.gd656killicon.client.KillIconRenderer;
import org.mods.gd656killicon.client.RenderHelper;
import org.mods.gd656killicon.events.ClientEventHandler;
import org.mods.gd656killicon.network.NetworkHandler;

public class Gd656killiconClient implements ClientModInitializer {
   private static final Map<Identifier, Long> soundCooldowns = new HashMap<>();
   private static final int SOUND_COOLDOWN_MS = 100;

   public void onInitializeClient() {
      Config.initialize();
      NetworkHandler.registerClientReceivers();
      ClientEventHandler.registerKeyMappings();
      RenderHelper renderHelper = new RenderHelper();
      renderHelper.loadResources();
      KillIconRenderer killIconRenderer = new KillIconRenderer();
      HudRenderCallback.EVENT.register(killIconRenderer);
      ClientEventHandler.registerClientTick();
      ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> org.mods.gd656killicon.client.ClientKillTracker.clear());
      if (FabricLoader.getInstance().isModLoaded("modmenu")) {
         System.out.println("ModMenu detected - configuration screen will be available in mod list");
      } else {
         System.out.println("ModMenu not found - configuration screen will only be available via keybind");
      }
   }

   public static void playSoundWithCooldown(SoundEvent sound, float volume) {
      if (Config.enableSoundEffects) {
         MinecraftClient mc = MinecraftClient.getInstance();
         if (mc.getSoundManager() != null) {
            Identifier soundName = Registries.SOUND_EVENT.getId(sound);
            if (soundName != null) {
               long currentTime = System.currentTimeMillis();
               Long lastPlayed = soundCooldowns.get(soundName);
               if (lastPlayed == null || currentTime - lastPlayed >= 100L) {
                  soundCooldowns.put(soundName, currentTime);
                  mc.getSoundManager()
                     .play(
                        new PositionedSoundInstance(
                           sound.id(),
                           SoundCategory.MASTER,
                           volume,
                           1.0F,
                           SoundInstance.createRandom(),
                           false,
                           0,
                           SoundInstance.AttenuationType.NONE,
                           0.0,
                           0.0,
                           0.0,
                           true
                        )
                     );
               }
            }
         }
      }
   }

   public static void cleanupExpiredSoundCooldowns() {
      long currentTime = System.currentTimeMillis();
      soundCooldowns.entrySet().removeIf(entry -> currentTime - entry.getValue() > 1000L);
   }
}
