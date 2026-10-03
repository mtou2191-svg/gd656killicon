package org.mods.gd656killicon.client.iconrenderer;

import net.minecraft.client.gl.RenderPipelines;
import java.util.Iterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix3x2fStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.mods.gd656killicon.Config;
import org.mods.gd656killicon.Gd656killicon;
import org.mods.gd656killicon.Gd656killiconClient;
import org.mods.gd656killicon.client.KillIconRenderer;
import org.mods.gd656killicon.client.RenderHelper;

@Environment(EnvType.CLIENT)
public class ScrollingIconRenderer {
   private static Identifier VANALLA_KILL_ICON;
   private static Identifier VANALLA_ULTIMATE_ICON;
   private static Identifier VANALLA_ASSIST_ICON;

   public void handleScrollingMode(String weaponName, String targetName, boolean isCritical, boolean playUltimateSound, boolean isAssist) {
      MinecraftClient mc = MinecraftClient.getInstance();
      int screenWidth = mc.getWindow().getScaledWidth();
      int screenHeight = mc.getWindow().getScaledHeight();
      int iconSize = getCurrentIconSize();
      RenderHelper.PositionConfig pos = this.getCurrentPosition();
      float targetX = screenWidth / 2.0F + pos.iconX;
      float targetY = screenHeight + pos.iconY;
      ScrollingIconRenderer.KillIconInstance newIcon = new ScrollingIconRenderer.KillIconInstance(
         System.currentTimeMillis(), targetX - iconSize / 2.0F, targetY, isCritical
      );
      newIcon.isAssist = isAssist;
      newIcon.prevX = targetX - iconSize / 2.0F;
      newIcon.currentX = targetX - iconSize / 2.0F;
      newIcon.targetX = targetX - iconSize / 2.0F;
      KillIconRenderer.latestIsCritical = isCritical;
      if (Config.enableSoundEffects) {
         float volume = Config.soundVolume / 100.0F;
         SoundEvent sound = isAssist ? getAssistKillSound() : getKillSound(playUltimateSound);
         Gd656killiconClient.playSoundWithCooldown(sound, volume);
      }

      KillIconRenderer.activeIcons.add(newIcon);
      KillIconRenderer.textAnimationStartTime = System.currentTimeMillis();
      KillIconRenderer.textHideTime = 0L;
      if (KillIconRenderer.activeIcons.size() > Config.forceHideCount) {
         this.startFadeOutSequence(System.currentTimeMillis());
      }

      this.checkMaxDisplayCount();
      this.updateAllIconTargetPositions();
      KillIconRenderer.lastKillTime = System.currentTimeMillis();
      KillIconRenderer.isFadingOutAll = false;
      KillIconRenderer.latestWeaponName = weaponName;
      KillIconRenderer.latestTargetName = targetName;
   }

   public void renderIcons(DrawContext guiGraphics, Matrix3x2fStack poseStack, long currentTime) {
      if (!KillIconRenderer.activeIcons.isEmpty()) {
         Iterator<ScrollingIconRenderer.KillIconInstance> iterator = KillIconRenderer.activeIcons.iterator();
         int iconSize = getCurrentIconSize();

         while (iterator.hasNext()) {
            ScrollingIconRenderer.KillIconInstance icon = iterator.next();
            long elapsed = currentTime - icon.startTime;
            updateIconAnimation(icon, elapsed, currentTime);
            if (icon.alpha <= 0.0F) {
               iterator.remove();
               this.updateAllIconTargetPositions();
               if (KillIconRenderer.activeIcons.isEmpty()) {
                  KillIconRenderer.isGroupDisplaying = false;
                  KillIconRenderer.shouldStartFadeOut = false;
                  KillIconRenderer.isFadingOutAll = false;
               }
            } else {
               updatePosition(icon, currentTime);
               if (icon.isCombo) {
                  ComboIconRenderer.renderComboIcon(guiGraphics, poseStack, icon, iconSize);
               } else {
                  renderSingleIcon(guiGraphics, poseStack, icon);
               }
            }
         }
      }
   }

   public void updateAllIconTargetPositions() {
      if (!KillIconRenderer.activeIcons.isEmpty()) {
         MinecraftClient mc = MinecraftClient.getInstance();
         int screenWidth = mc.getWindow().getScaledWidth();
         int iconSize = getCurrentIconSize();
         RenderHelper.PositionConfig pos = this.getCurrentPosition();
         float centerX = screenWidth / 2.0F + pos.iconX;
         float iconSpacing = iconSize + Config.killIconSpacing;

         for (int i = 0; i < KillIconRenderer.activeIcons.size(); i++) {
            ScrollingIconRenderer.KillIconInstance icon = KillIconRenderer.activeIcons.get(i);
            float position = i - (KillIconRenderer.activeIcons.size() - 1) / 2.0F;
            float newTargetX = centerX + position * iconSpacing - iconSize / 2.0F;
            if (Math.abs(icon.targetX - newTargetX) > 1.0F) {
               icon.prevX = icon.currentX;
               icon.targetX = newTargetX;
               icon.positionAnimationStart = System.currentTimeMillis();
            }
         }
      }
   }

   public void startFadeOutSequence(long currentTime) {
      KillIconRenderer.isFadingOutAll = true;
      int totalIcons = KillIconRenderer.activeIcons.size();
      float speedFactor = calculateFadeOutSpeedFactor(totalIcons);
      long baseDelay = (long)(40.0F / speedFactor);
      long fadeDuration = 100L;
      baseDelay = Math.max(baseDelay, 20L);

      for (int i = 0; i < totalIcons; i++) {
         ScrollingIconRenderer.KillIconInstance icon = KillIconRenderer.activeIcons.get(i);
         icon.fadeStartTime = currentTime + i * baseDelay;
         icon.fadeDuration = fadeDuration;
      }
   }

   public void loadResources() {
      String stylePath = Config.iconStyle == Config.IconStyle.VANILLA ? "vanilla_gui" : "modern_gui";
      VANALLA_KILL_ICON = Identifier.of("gd656killicon", "textures/gui/" + stylePath + "/rollingmode/kill_icon.png");
      VANALLA_ULTIMATE_ICON = Identifier.of("gd656killicon", "textures/gui/" + stylePath + "/rollingmode/ultimate_icon.png");
      VANALLA_ASSIST_ICON = Identifier.of("gd656killicon", "textures/gui/" + stylePath + "/rollingmode/assist_icon.png");
   }

   private void checkMaxDisplayCount() {
      if (KillIconRenderer.activeIcons.size() > Config.maxDisplayCount) {
         KillIconRenderer.activeIcons.remove(0);
         this.updateAllIconTargetPositions();
      }
   }

   private static void renderSingleIcon(DrawContext guiGraphics, Matrix3x2fStack poseStack, ScrollingIconRenderer.KillIconInstance icon) {
      int currentIconSize = getCurrentIconSize();
      int scaledSize = (int)(currentIconSize * icon.scale);
      int offsetX = (currentIconSize - scaledSize) / 2;
      int offsetY = (currentIconSize - scaledSize) / 2;
      poseStack.pushMatrix();
      poseStack.translate(icon.currentX + offsetX, icon.baseY + offsetY);
      poseStack.scale(icon.scale, icon.scale);
      ScrollingIconRenderer.IconType iconType = getIconType(icon);
      Identifier iconTexture;
      switch (iconType) {
         case ASSIST:
            iconTexture = VANALLA_ASSIST_ICON;
            break;
         case COMBO:
            poseStack.popMatrix();
            ComboIconRenderer.renderComboIcon(guiGraphics, poseStack, icon, currentIconSize);
            return;
         case CRITICAL:
            iconTexture = VANALLA_ULTIMATE_ICON;
            break;
         default:
            iconTexture = VANALLA_KILL_ICON;
      }

      int alpha = (int)(icon.alpha * 255.0F) & 255;
      int brightness = (int)MathHelper.clamp(icon.brightness * 255.0F, 0.0F, 255.0F) & 255;
      int color = alpha << 24 | brightness << 16 | brightness << 8 | brightness;
      guiGraphics.drawTexture(
         RenderPipelines.GUI_TEXTURED,
         iconTexture,
         0,
         0,
         0.0F,
         0.0F,
         currentIconSize,
         currentIconSize,
         currentIconSize,
         currentIconSize,
         currentIconSize,
         currentIconSize,
         color
      );
      poseStack.popMatrix();
   }

   private static void updateIconAnimation(ScrollingIconRenderer.KillIconInstance icon, long elapsed, long currentTime) {
      if (elapsed < Config.killIconAnimationDuration) {
         float progress = MathHelper.clamp((float)elapsed / Config.killIconAnimationDuration, 0.0F, 1.0F);
         float easedProgress = 1.0F - (float)Math.pow(1.0F - progress, 3.0);
         icon.scale = MathHelper.lerp(easedProgress, 1.5F, 1.0F);
         icon.brightness = MathHelper.lerp(easedProgress, 4.0F, 1.0F);
      }

      if (icon.fadeStartTime != -1L && currentTime >= icon.fadeStartTime) {
         long fadeElapsed = currentTime - icon.fadeStartTime;
         float fadeProgress = Math.min((float)fadeElapsed / 200.0F, 1.0F);
         icon.alpha = 1.0F - fadeProgress;
         if (fadeProgress >= 1.0F) {
            icon.alpha = 0.0F;
         }
      }

      if (KillIconRenderer.isFadingOutAll && icon.fadeStartTime == -1L) {
         icon.fadeStartTime = currentTime;
         icon.fadeDuration = 200L;
      }
   }

   private static void updatePosition(ScrollingIconRenderer.KillIconInstance icon, long currentTime) {
      if (Math.abs(icon.currentX - icon.targetX) > 0.1F) {
         long moveElapsed = currentTime - icon.positionAnimationStart;
         float progress = Math.min((float)moveElapsed / 300.0F, 1.0F);
         float easedProgress = 1.0F - (1.0F - progress) * (1.0F - progress);
         icon.currentX = MathHelper.lerp(easedProgress, icon.prevX, icon.targetX);
      }
   }

   private static int getCurrentIconSize() {
      return switch (Config.iconMode) {
         case SCROLLING -> Config.scrollIconSize;
         case COMBO -> Config.comboIconSize;
         case CARD -> Config.cardIconSize;
      };
   }

   private static ScrollingIconRenderer.IconType getIconType(ScrollingIconRenderer.KillIconInstance icon) {
      if (icon.isAssist) {
         return ScrollingIconRenderer.IconType.ASSIST;
      } else if (icon.isCombo) {
         return ScrollingIconRenderer.IconType.COMBO;
      } else {
         return icon.isCritical ? ScrollingIconRenderer.IconType.CRITICAL : ScrollingIconRenderer.IconType.NORMAL;
      }
   }

   private RenderHelper.PositionConfig getCurrentPosition() {
      RenderHelper.PositionConfig config = new RenderHelper.PositionConfig();
      switch (Config.iconMode) {
         case SCROLLING:
            config.iconX = Config.scrollIconX;
            config.iconY = Config.scrollIconY;
            break;
         case COMBO:
            config.iconX = Config.comboIconX;
            config.iconY = Config.comboIconY;
            break;
         case CARD:
            config.iconX = Config.cardIconX;
            config.iconY = Config.cardIconY;
      }

      return config;
   }

   private static SoundEvent getKillSound(boolean isUltimate) {
      if (Config.iconStyle == Config.IconStyle.VANILLA) {
         return isUltimate ? Gd656killicon.ULTIMATE_KILL_VANILLA : Gd656killicon.KILL_SOUND_VANILLA;
      } else {
         return isUltimate ? Gd656killicon.ULTIMATE_KILL_MODERN : Gd656killicon.KILL_SOUND_MODERN;
      }
   }

   private static SoundEvent getAssistKillSound() {
      return Config.iconStyle == Config.IconStyle.VANILLA ? Gd656killicon.ASSIST_KILL_VANILLA : Gd656killicon.ASSIST_KILL_MODERN;
   }

   private static float calculateFadeOutSpeedFactor(int iconCount) {
      if (iconCount <= 3) {
         return 0.4F;
      } else if (iconCount <= 6) {
         return 0.5F;
      } else if (iconCount <= 10) {
         return 0.8F;
      } else if (iconCount <= 15) {
         return 1.0F;
      } else {
         return iconCount <= 20 ? 2.0F : 3.0F;
      }
   }

   private static enum IconType {
      ASSIST,
      COMBO,
      CRITICAL,
      NORMAL;
   }

   public static class KillIconInstance {
      public long startTime;
      public float brightness;
      public float scale;
      public float alpha;
      public float prevX;
      public float currentX;
      public float targetX;
      public float baseY;
      public long positionAnimationStart;
      public long fadeStartTime = -1L;
      public long fadeDuration = 1L;
      public boolean isCritical;
      public boolean isCombo;
      public int comboIndex;
      public boolean isAssist;
      public float currentY;
      public float targetY;
      public long animationStartTime;
      public int currentFrame;
      public boolean isAnimating;

      public KillIconInstance(long startTime, float initialX, float baseY, boolean isCritical) {
         this.startTime = startTime;
         this.baseY = baseY;
         this.currentY = baseY;
         this.targetY = baseY;
         this.brightness = 4.0F;
         this.scale = 1.5F;
         this.alpha = 1.0F;
         this.prevX = initialX;
         this.currentX = initialX;
         this.targetX = initialX;
         this.positionAnimationStart = System.currentTimeMillis();
         this.isCritical = isCritical;
         this.isAssist = false;
         this.isCombo = false;
         this.comboIndex = 0;
         this.animationStartTime = startTime;
         this.currentFrame = 0;
         this.isAnimating = false;
      }
   }
}
