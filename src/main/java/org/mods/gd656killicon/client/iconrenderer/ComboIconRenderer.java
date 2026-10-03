package org.mods.gd656killicon.client.iconrenderer;

import net.minecraft.client.gl.RenderPipelines;
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
public class ComboIconRenderer {
   private static Config.IconStyle currentIconStyle = null;
   public static Identifier[] VANALLA_COMBO_ICONS;

   public static void handleComboMode(String weaponName, String targetName, boolean isCritical, boolean isAssist) {
      MinecraftClient mc = MinecraftClient.getInstance();
      KillIconRenderer.activeIcons.clear();
      int screenWidth = mc.getWindow().getScaledWidth();
      int screenHeight = mc.getWindow().getScaledHeight();
      int iconSize = Config.killIconSize;
      RenderHelper.PositionConfig pos = getCurrentPosition();
      float targetX = screenWidth / 2.0F + pos.iconX;
      float targetY = screenHeight + pos.iconY;
      int comboIndex = Math.min(KillIconRenderer.comboCount, 6) - 1;
      comboIndex = Math.max(comboIndex, 0);
      ScrollingIconRenderer.KillIconInstance newIcon = new ScrollingIconRenderer.KillIconInstance(
         System.currentTimeMillis(), targetX - iconSize / 2.0F, targetY - iconSize / 2.0F, isCritical
      );
      newIcon.isCombo = true;
      newIcon.comboIndex = comboIndex;
      newIcon.isAssist = isAssist;
      if (Config.comboIconAnimationEnabled) {
         newIcon.animationStartTime = System.currentTimeMillis();
         newIcon.currentFrame = 0;
         newIcon.isAnimating = true;
         newIcon.scale = 1.8F;
         newIcon.brightness = 2.0F;
      } else {
         newIcon.isAnimating = false;
         newIcon.currentFrame = Config.comboIconAnimationTotalFrames - 1;
         newIcon.scale = 1.0F;
         newIcon.brightness = 1.0F;
      }

      KillIconRenderer.latestIsCritical = isCritical;
      SoundEvent[] comboSounds = getComboSounds();
      if (comboIndex < comboSounds.length) {
         try {
            SoundEvent sound = comboSounds[comboIndex];
            float volume = Config.soundVolume / 100.0F;
            Gd656killiconClient.playSoundWithCooldown(sound, volume);
         } catch (Exception var16) {
         }
      }

      KillIconRenderer.activeIcons.add(newIcon);
      KillIconRenderer.textAnimationStartTime = System.currentTimeMillis();
      KillIconRenderer.textHideTime = 0L;
      KillIconRenderer.lastKillTime = System.currentTimeMillis();
      KillIconRenderer.isFadingOutAll = false;
      KillIconRenderer.latestWeaponName = weaponName;
      KillIconRenderer.latestTargetName = targetName;
   }

   public static void renderComboIcon(DrawContext guiGraphics, Matrix3x2fStack poseStack, ScrollingIconRenderer.KillIconInstance icon, int iconSize) {
      if (icon.comboIndex >= 0 && icon.comboIndex < VANALLA_COMBO_ICONS.length) {
         if (icon.isAnimating) {
            updateAnimationFrame(icon);
         }

         int scaledSize = (int)(iconSize * icon.scale);
         int offsetX = (iconSize - scaledSize) / 2;
         int offsetY = (iconSize - scaledSize) / 2;
         poseStack.pushMatrix();
         poseStack.translate(icon.currentX + offsetX, icon.currentY + offsetY);
         poseStack.scale(icon.scale, icon.scale);
         Identifier iconTexture = VANALLA_COMBO_ICONS[icon.comboIndex];
         if (iconTexture == null) {
            poseStack.popMatrix();
            return;
         }

         int alpha = (int)(icon.alpha * 255.0F) & 255;
         int brightness = (int)MathHelper.clamp(icon.brightness * 255.0F, 0.0F, 255.0F) & 255;
         int color = alpha << 24 | brightness << 16 | brightness << 8 | brightness;
         if (icon.isAnimating) {
            renderAnimationFrame(guiGraphics, iconTexture, icon, iconSize, color);
         } else {
            renderStaticFrame(guiGraphics, iconTexture, iconSize, color);
         }

         poseStack.popMatrix();
      }
   }

   public static void loadResources() {
      if (currentIconStyle != Config.iconStyle) {
         String stylePath = Config.iconStyle == Config.IconStyle.VANILLA ? "vanilla_gui" : "modern_gui";
         VANALLA_COMBO_ICONS = new Identifier[6];

         for (int i = 0; i < 6; i++) {
            VANALLA_COMBO_ICONS[i] = Identifier.of("gd656killicon", "textures/gui/" + stylePath + "/combomode/combo_" + (i + 1) + ".png");
         }

         currentIconStyle = Config.iconStyle;
      }
   }

   private static void updateAnimationFrame(ScrollingIconRenderer.KillIconInstance icon) {
      if (icon.isAnimating) {
         long currentTime = System.currentTimeMillis();
         long elapsed = currentTime - icon.animationStartTime;
         float progress = MathHelper.clamp((float)elapsed / Config.comboIconAnimationDuration, 0.0F, 1.0F);
         int targetFrame = (int)(progress * (Config.comboIconAnimationTotalFrames - 1));
         if (targetFrame >= Config.comboIconAnimationTotalFrames - 1) {
            icon.isAnimating = false;
            icon.currentFrame = Config.comboIconAnimationTotalFrames - 1;
         } else {
            icon.currentFrame = targetFrame;
         }
      }
   }

   private static void renderAnimationFrame(DrawContext guiGraphics, Identifier texture, ScrollingIconRenderer.KillIconInstance icon, int iconSize, int color) {
      if (texture != null) {
         int textureHeight = iconSize * Config.comboIconAnimationTotalFrames;
         int frameX = 0;
         int frameY = icon.currentFrame * iconSize;
         guiGraphics.drawTexture(
            RenderPipelines.GUI_TEXTURED, texture, 0, 0, (float)frameX, (float)frameY, iconSize, iconSize, iconSize, iconSize, iconSize, textureHeight, color
         );
      }
   }

   private static void renderStaticFrame(DrawContext guiGraphics, Identifier texture, int iconSize, int color) {
      if (texture != null) {
         int textureHeight = iconSize * Config.comboIconAnimationTotalFrames;
         int frameX = 0;
         int frameY = (Config.comboIconAnimationTotalFrames - 1) * iconSize;
         guiGraphics.drawTexture(
            RenderPipelines.GUI_TEXTURED, texture, 0, 0, (float)frameX, (float)frameY, iconSize, iconSize, iconSize, iconSize, iconSize, textureHeight, color
         );
      }
   }

   private static SoundEvent[] getComboSounds() {
      return Config.iconStyle == Config.IconStyle.VANILLA
         ? new SoundEvent[]{
            Gd656killicon.COMBO_1_VANILLA,
            Gd656killicon.COMBO_2_VANILLA,
            Gd656killicon.COMBO_3_VANILLA,
            Gd656killicon.COMBO_4_VANILLA,
            Gd656killicon.COMBO_5_VANILLA,
            Gd656killicon.COMBO_6_VANILLA
         }
         : new SoundEvent[]{
            Gd656killicon.COMBO_1_MODERN,
            Gd656killicon.COMBO_2_MODERN,
            Gd656killicon.COMBO_3_MODERN,
            Gd656killicon.COMBO_4_MODERN,
            Gd656killicon.COMBO_5_MODERN,
            Gd656killicon.COMBO_6_MODERN
         };
   }

   private static RenderHelper.PositionConfig getCurrentPosition() {
      RenderHelper.PositionConfig config = new RenderHelper.PositionConfig();
      config.iconX = Config.comboIconX;
      config.iconY = Config.comboIconY;
      config.subtitleX = Config.comboSubtitleX;
      config.subtitleY = Config.comboSubtitleY;
      config.scoreX = Config.comboScoreX;
      config.scoreY = Config.comboScoreY;
      config.bonusX = Config.comboBonusX;
      config.bonusY = Config.comboBonusY;
      return config;
   }
}
