package org.mods.gd656killicon.client.subtitlerenderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix3x2fStack;
import net.minecraft.util.math.MathHelper;
import org.mods.gd656killicon.Config;
import org.mods.gd656killicon.client.KillIconRenderer;
import org.mods.gd656killicon.client.ModConfigScreen;
import org.mods.gd656killicon.client.RenderHelper;

@Environment(EnvType.CLIENT)
public class ComboScoreRenderer {
   private static final long NUMBER_ANIMATION_UPDATE_INTERVAL = 66L;
   private static final long SCORE_ANIMATION_DURATION = 200L;
   private String cachedScoreText = "";
   private RenderHelper.PositionConfig cachedPositionConfig;
   private int lastDisplayedScore = -1;
   private long lastConfigCheckTime;
   private long lastNumberAnimationUpdate;
   private final ScoreItemRenderer scoreItemRenderer = new ScoreItemRenderer();

   public void renderComboScore(DrawContext guiGraphics, long currentTime) {
      if (RenderHelper.isElementVisible(ModConfigScreen.ElementType.SCORE)) {
         MinecraftClient mc = MinecraftClient.getInstance();
         TextRenderer font = mc.textRenderer;
         int screenWidth = mc.getWindow().getScaledWidth();
         int screenHeight = mc.getWindow().getScaledHeight();
         RenderHelper.PositionConfig pos = this.getCurrentPosition();
         this.updateScoreAnimation(currentTime);
         this.updateScoreItems(currentTime);
         int scoreY = screenHeight + pos.scoreY;
         long timeSinceLastDamage = currentTime - KillIconRenderer.lastDamageTime;
         if (timeSinceLastDamage > Config.comboTimeoutMs) {
            this.resetComboScore();
         } else {
            float scoreAlpha = this.calculateScoreAlpha(currentTime);
            boolean shouldFlash = this.shouldFlash(currentTime, timeSinceLastDamage);
            if (this.lastDisplayedScore != KillIconRenderer.displayedComboScore) {
               this.cachedScoreText = String.valueOf(KillIconRenderer.displayedComboScore);
               this.lastDisplayedScore = KillIconRenderer.displayedComboScore;
            }

            int textWidth = font.getWidth(this.cachedScoreText);
            int textX = screenWidth / 2 + pos.scoreX - textWidth / 2;
            float scoreSizeScale = RenderHelper.getElementSize(ModConfigScreen.ElementType.SCORE) / 100.0F;
            int scoreColor = this.getScoreColor(shouldFlash);
            Matrix3x2fStack poseStack = guiGraphics.getMatrices();
            poseStack.pushMatrix();
            float centerX = textX + textWidth / 2.0F;
            float centerY = scoreY + 9.0F / 2.0F;
            poseStack.translate(centerX, centerY);
            poseStack.scale(scoreSizeScale, scoreSizeScale);
            poseStack.translate(-centerX, -centerY);
            float[] animationValues = this.calculateAnimationValues(currentTime);
            float animScale = animationValues[0];
            float brightness = animationValues[1];
            poseStack.pushMatrix();
            poseStack.translate(centerX, centerY);
            poseStack.scale(animScale, animScale);
            poseStack.translate(-centerX, -centerY);
            int finalColor = this.applyColorWithBrightness(scoreColor, brightness, scoreAlpha);
            guiGraphics.drawText(font, this.cachedScoreText, textX, scoreY, finalColor, true);
            poseStack.popMatrix();
            poseStack.popMatrix();
            this.scoreItemRenderer.renderScoreItems(guiGraphics, currentTime);
         }
      }
   }

   private void resetComboScore() {
      KillIconRenderer.comboScore = 0;
      KillIconRenderer.displayedComboScore = 0;
      KillIconRenderer.targetComboScore = 0;
      KillIconRenderer.isComboScoreVisible = false;
      KillIconRenderer.scoreItems.clear();
      KillIconRenderer.activeScoreItems.clear();
      KillIconRenderer.isFlashing = false;
      KillIconRenderer.flashCount = 0;
      KillIconRenderer.lastKillTime = 0L;
      this.lastDisplayedScore = -1;
      this.lastNumberAnimationUpdate = 0L;
   }

   private void updateScoreAnimation(long currentTime) {
      if (currentTime - this.lastNumberAnimationUpdate >= 66L) {
         this.lastNumberAnimationUpdate = currentTime;
         if (KillIconRenderer.displayedComboScore != KillIconRenderer.targetComboScore) {
            long elapsed = currentTime - KillIconRenderer.lastScoreUpdateTime;
            if (elapsed >= 200L) {
               KillIconRenderer.displayedComboScore = KillIconRenderer.targetComboScore;
            } else {
               float progress = (float)elapsed / 200.0F;
               int startScore = KillIconRenderer.scoreAnimationStart;
               int endScore = KillIconRenderer.targetComboScore;
               KillIconRenderer.displayedComboScore = startScore + Math.round((endScore - startScore) * progress);
            }
         }

         if (currentTime - KillIconRenderer.lastDamageTime > Config.comboTimeoutMs && KillIconRenderer.isComboScoreVisible) {
            this.resetComboScore();
         }
      }
   }

   private void updateScoreItems(long currentTime) {
      this.scoreItemRenderer.updateScoreItems(currentTime);
   }

   private int applyColorWithBrightness(int color, float brightness, float alpha) {
      int a = (int)((color >> 24 & 0xFF) * alpha);
      int r = (int)((color >> 16 & 0xFF) * brightness);
      int g = (int)((color >> 8 & 0xFF) * brightness);
      int b = (int)((color & 0xFF) * brightness);
      r = MathHelper.clamp(r, 0, 255);
      g = MathHelper.clamp(g, 0, 255);
      b = MathHelper.clamp(b, 0, 255);
      a = MathHelper.clamp(a, 0, 255);
      return a << 24 | r << 16 | g << 8 | b;
   }

   private float[] calculateAnimationValues(long currentTime) {
      float animScale = 1.0F;
      float brightness = 1.0F;
      long elapsed = currentTime - KillIconRenderer.comboScoreShowTime;
      if (elapsed < 300L) {
         float progress = MathHelper.clamp((float)elapsed / 300.0F, 0.0F, 1.0F);
         float easedProgress = progress * progress;
         animScale = 1.0F + 0.5F * (1.0F - easedProgress);
         brightness = 1.0F + 3.0F * (1.0F - easedProgress);
      }

      return new float[]{animScale, brightness};
   }

   private float calculateScoreAlpha(long currentTime) {
      long elapsed = currentTime - KillIconRenderer.comboScoreShowTime;
      if (elapsed < 1000L) {
         float progress = MathHelper.clamp((float)elapsed / 1000.0F, 0.0F, 1.0F);
         if (progress < 0.2F) {
            return System.currentTimeMillis() % 200L > 100L ? 1.0F : 0.5F;
         }
      }

      return 1.0F;
   }

   private RenderHelper.PositionConfig getCurrentPosition() {
      long currentTime = System.currentTimeMillis();
      if (this.cachedPositionConfig == null || currentTime - this.lastConfigCheckTime > 1000L) {
         this.cachedPositionConfig = this.createPositionConfig();
         this.lastConfigCheckTime = currentTime;
      }

      return this.cachedPositionConfig;
   }

   private int getScoreColor(boolean shouldFlash) {
      if (shouldFlash) {
         return Config.flashingColor;
      } else {
         return KillIconRenderer.comboScore >= 1000 ? Config.highScoreColor : -1;
      }
   }

   private boolean shouldFlash(long currentTime, long timeSinceLastDamage) {
      if (timeSinceLastDamage > Config.comboTimeoutMs - 1500 && timeSinceLastDamage <= Config.comboTimeoutMs) {
         if (!KillIconRenderer.isFlashing) {
            KillIconRenderer.isFlashing = true;
            KillIconRenderer.flashCount = 0;
            KillIconRenderer.lastFlashTime = currentTime;
         }

         if (currentTime - KillIconRenderer.lastFlashTime > 375L) {
            KillIconRenderer.flashCount++;
            KillIconRenderer.lastFlashTime = currentTime;
            if (KillIconRenderer.flashCount >= 4) {
               KillIconRenderer.isFlashing = false;
            }
         }

         return KillIconRenderer.isFlashing && KillIconRenderer.flashCount % 2 == 0;
      } else {
         KillIconRenderer.isFlashing = false;
         return false;
      }
   }

   private RenderHelper.PositionConfig createPositionConfig() {
      RenderHelper.PositionConfig config = new RenderHelper.PositionConfig();
      switch (Config.iconMode) {
         case CARD:
            config.iconX = Config.cardIconX;
            config.iconY = Config.cardIconY;
            config.subtitleX = Config.cardSubtitleX;
            config.subtitleY = Config.cardSubtitleY;
            config.scoreX = Config.cardScoreX;
            config.scoreY = Config.cardScoreY;
            config.bonusX = Config.cardBonusX;
            config.bonusY = Config.cardBonusY;
            break;
         case COMBO:
            config.iconX = Config.comboIconX;
            config.iconY = Config.comboIconY;
            config.subtitleX = Config.comboSubtitleX;
            config.subtitleY = Config.comboSubtitleY;
            config.scoreX = Config.comboScoreX;
            config.scoreY = Config.comboScoreY;
            config.bonusX = Config.comboBonusX;
            config.bonusY = Config.comboBonusY;
            break;
         case SCROLLING:
            config.iconX = Config.scrollIconX;
            config.iconY = Config.scrollIconY;
            config.subtitleX = Config.scrollSubtitleX;
            config.subtitleY = Config.scrollSubtitleY;
            config.scoreX = Config.scrollScoreX;
            config.scoreY = Config.scrollScoreY;
            config.bonusX = Config.scrollBonusX;
            config.bonusY = Config.scrollBonusY;
      }

      return config;
   }
}
