package org.mods.gd656killicon.client.subtitlerenderer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix3x2fStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.mods.gd656killicon.Config;
import org.mods.gd656killicon.client.KillIconRenderer;
import org.mods.gd656killicon.client.ModConfigScreen;
import org.mods.gd656killicon.client.RenderHelper;

@Environment(EnvType.CLIENT)
public class ScoreItemRenderer {
   private static final long COMBO_KILL_ANIMATION_DURATION = 500L;
   private static final long ANIMATION_DURATION = 500L;
   private static final float ANIMATION_SPEED_MULTIPLIER = 0.5F;
   private static final int BATCH_SIZE = 10;
   private static final long NUMBER_ANIMATION_UPDATE_INTERVAL = 66L;
   private static final long ENTRY_ANIMATION_UPDATE_INTERVAL = 33L;
   private static final StringBuilder textBuilder = new StringBuilder(64);
   private RenderHelper.PositionConfig cachedPositionConfig;
   private long lastConfigCheckTime;
   private long lastNumberAnimationUpdate;
   private long lastEntryAnimationUpdate;
   private final List<ScoreItemRenderer.CachedText> textCache = new ArrayList<>(10);

   public void updateScoreItems(long currentTime) {
      if (!KillIconRenderer.scoreItems.isEmpty()) {
         int processed = 0;

         while (!KillIconRenderer.scoreItems.isEmpty() && processed < 10) {
            ScoreItemRenderer.ScoreItem item = KillIconRenderer.scoreItems.poll();
            if (item != null) {
               item.yOffset = 15.0F;
               item.targetYOffset = 0.0F;
               item.isAnimating = true;
               item.animationStartTime = currentTime;
               KillIconRenderer.activeScoreItems.add(item);
               processed++;
            }
         }
      }

      if (!KillIconRenderer.activeScoreItems.isEmpty()) {
         this.updateNumberAnimations(currentTime);
         this.updateEntryAnimations(currentTime);
         this.updatePositions(currentTime);
      }
   }

   public void renderScoreItems(DrawContext guiGraphics, long currentTime) {
      if (RenderHelper.isElementVisible(ModConfigScreen.ElementType.BONUS) && !KillIconRenderer.activeScoreItems.isEmpty()) {
         MinecraftClient mc = MinecraftClient.getInstance();
         TextRenderer font = mc.textRenderer;
         int screenWidth = mc.getWindow().getScaledWidth();
         int screenHeight = mc.getWindow().getScaledHeight();
         RenderHelper.PositionConfig pos = this.getCurrentPosition();
         int bonusBaseY = screenHeight + pos.bonusY;
         float bonusSizeScale = RenderHelper.getElementSize(ModConfigScreen.ElementType.BONUS) / 100.0F;
         this.textCache.clear();
         this.preprocessRenderData(font, screenWidth, bonusBaseY, bonusSizeScale, currentTime);
         this.renderCachedTexts(guiGraphics, font);
      }
   }

   private void updateNumberAnimations(long currentTime) {
      if (currentTime - this.lastNumberAnimationUpdate >= 66L) {
         this.lastNumberAnimationUpdate = currentTime;

         for (ScoreItemRenderer.ScoreItem item : KillIconRenderer.activeScoreItems) {
            item.updateNumberAnimation(currentTime);
            item.lastUpdateTime = currentTime;
         }
      }
   }

   private void updateEntryAnimations(long currentTime) {
      if (currentTime - this.lastEntryAnimationUpdate >= 33L) {
         this.lastEntryAnimationUpdate = currentTime;

         for (ScoreItemRenderer.ScoreItem item : KillIconRenderer.activeScoreItems) {
            item.updateEntryAnimation(currentTime);
         }
      }
   }

   private void updatePositions(long currentTime) {
      float animationSpeed = (float)(0.1 * (0.2 / Config.scrollAnimationSpeed)) * 0.5F;
      Iterator<ScoreItemRenderer.ScoreItem> iterator = KillIconRenderer.activeScoreItems.iterator();

      while (iterator.hasNext()) {
         ScoreItemRenderer.ScoreItem item = iterator.next();
         long elapsed = currentTime - item.startTime;
         if (item.isComboKill) {
            long timeSinceLastDamage = currentTime - KillIconRenderer.lastDamageTime;
            if (timeSinceLastDamage > Config.comboTimeoutMs) {
               iterator.remove();
               KillIconRenderer.comboKillItem = null;
               continue;
            }
         }

         if (elapsed > item.displayDuration || item.quickDisappearStartTime > 0L && currentTime - item.quickDisappearStartTime > 200L) {
            iterator.remove();
            if (item.isComboKill) {
               KillIconRenderer.comboKillItem = null;
            }
         } else if (Math.abs(item.yOffset - item.targetYOffset) > 0.1F) {
            item.yOffset = MathHelper.lerp(animationSpeed, item.yOffset, item.targetYOffset);
         }
      }

      this.adjustItemPositions();
   }

   private void preprocessRenderData(TextRenderer font, int screenWidth, int bonusBaseY, float bonusSizeScale, long currentTime) {
      Iterator<ScoreItemRenderer.ScoreItem> iterator = KillIconRenderer.activeScoreItems.iterator();

      while (iterator.hasNext()) {
         ScoreItemRenderer.ScoreItem item = iterator.next();
         long elapsed = currentTime - item.startTime;
         if (elapsed > item.displayDuration) {
            iterator.remove();
            if (item.isComboKill) {
               KillIconRenderer.comboKillItem = null;
            }
         } else {
            Text textComponent = item.getCachedComponent();
            int textWidth = item.getTextWidth(font);
            int textY = (int)(bonusBaseY + item.yOffset + item.animationOffset);
            int textX = screenWidth / 2 + this.getCurrentPosition().bonusX - textWidth / 2;
            int baseColor = -1;
            if (item.alpha < 1.0F) {
               int alpha = (int)(item.alpha * 255.0F);
               baseColor = alpha << 24 | baseColor & 16777215;
            }

            this.textCache.add(new ScoreItemRenderer.CachedText(textComponent, textX, textY, baseColor, bonusSizeScale));
         }
      }
   }

   private void renderCachedTexts(DrawContext guiGraphics, TextRenderer font) {
      Matrix3x2fStack poseStack = guiGraphics.getMatrices();

      for (ScoreItemRenderer.CachedText cached : this.textCache) {
         int textWidth = font.getWidth(cached.component);
         float centerX = cached.x + textWidth / 2.0F;
         float centerY = cached.y + 9.0F / 2.0F;
         poseStack.pushMatrix();
         poseStack.translate(centerX, centerY);
         poseStack.scale(cached.scale, cached.scale);
         poseStack.translate(-centerX, -centerY);
         guiGraphics.drawText(font, cached.component, cached.x, cached.y, cached.color, true);
         poseStack.popMatrix();
      }
   }

   private void adjustItemPositions() {
      float prevOffset = 0.0F;

      for (int i = 0; i < KillIconRenderer.activeScoreItems.size(); i++) {
         ScoreItemRenderer.ScoreItem item = KillIconRenderer.activeScoreItems.get(i);
         if (i == 0) {
            item.targetYOffset = 0.0F;
         } else {
            float expectedOffset = prevOffset + Config.bonusLineSpacing;
            if (Math.abs(item.targetYOffset - expectedOffset) > 0.1F) {
               item.targetYOffset = expectedOffset;
            }
         }

         prevOffset = item.targetYOffset;
      }
   }

   private RenderHelper.PositionConfig getCurrentPosition() {
      long currentTime = System.currentTimeMillis();
      if (this.cachedPositionConfig == null || currentTime - this.lastConfigCheckTime > 1000L) {
         this.cachedPositionConfig = this.createPositionConfig();
         this.lastConfigCheckTime = currentTime;
      }

      return this.cachedPositionConfig;
   }

   private static String convertToMinecraftFormat(String text) {
      int length = text.length();
      textBuilder.setLength(0);

      for (int i = 0; i < length; i++) {
         char c = text.charAt(i);
         if (c == '[' && i + 2 < length && text.charAt(i + 2) == ']') {
            char colorChar = text.charAt(i + 1);
            if (isHexDigit(colorChar)) {
               textBuilder.append('§').append(colorChar);
               i += 2;
               continue;
            }
         }

         textBuilder.append(c);
      }

      return textBuilder.toString();
   }

   private static boolean isHexDigit(char c) {
      return c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F';
   }

   private RenderHelper.PositionConfig createPositionConfig() {
      RenderHelper.PositionConfig config = new RenderHelper.PositionConfig();
      switch (Config.iconMode) {
         case SCROLLING:
            config.iconX = Config.scrollIconX;
            config.iconY = Config.scrollIconY;
            config.subtitleX = Config.scrollSubtitleX;
            config.subtitleY = Config.scrollSubtitleY;
            config.scoreX = Config.scrollScoreX;
            config.scoreY = Config.scrollScoreY;
            config.bonusX = Config.scrollBonusX;
            config.bonusY = Config.scrollBonusY;
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
         case CARD:
            config.iconX = Config.cardIconX;
            config.iconY = Config.cardIconY;
            config.subtitleX = Config.cardSubtitleX;
            config.subtitleY = Config.cardSubtitleY;
            config.scoreX = Config.cardScoreX;
            config.scoreY = Config.cardScoreY;
            config.bonusX = Config.cardBonusX;
            config.bonusY = Config.cardBonusY;
      }

      return config;
   }

   private record CachedText(Text component, int x, int y, int color, float scale) {
   }

   public static class ScoreItem {
      public long quickDisappearStartTime = 0L;
      public float alpha = 1.0F;
      public final String baseText;
      public float currentPoints;
      public float targetPoints;
      public long startTime;
      public float yOffset;
      public float targetYOffset;
      public long lastUpdateTime;
      public String distance;
      public boolean isLongRange;
      public long displayDuration;
      public long animationStartTime;
      public float initialPoints;
      public float animationOffset;
      public boolean isAnimating;
      public boolean isComboKill = false;
      public int comboNumber = 0;
      public int displayComboNumber = 0;
      public boolean isAnimatingCombo = false;
      private String cachedText;
      private boolean textDirty = true;
      private Text cachedComponent;
      private int cachedTextWidth = -1;

      public ScoreItem(String baseText, float points, long startTime, boolean isLongRange) {
         this.baseText = baseText;
         this.currentPoints = 0.0F;
         this.targetPoints = points;
         this.initialPoints = 0.0F;
         this.startTime = startTime;
         this.lastUpdateTime = startTime;
         this.animationStartTime = startTime;
         this.distance = null;
         this.isLongRange = isLongRange;
         this.displayDuration = isLongRange ? 3000L : Config.comboScoreDuration;
         this.animationOffset = 8.0F;
         this.isAnimating = true;
      }

      public String getText() {
         if (!this.textDirty && this.cachedText != null) {
            return this.cachedText;
         } else {
            String pointsStr;
            if (this.targetPoints < 1.0F) {
               pointsStr = String.format("%.1f", this.currentPoints);
            } else {
               pointsStr = String.format("%.0f", this.currentPoints);
            }

            if (this.isComboKill) {
               String displayFormat = Config.comboBonusDisplay;
               this.cachedText = displayFormat.replace("{score}", pointsStr).replace("{combo}", String.valueOf(this.comboNumber));
            } else if (this.isLongRange && this.distance != null) {
               String displayFormat = Config.longrangeBonusDisplay;
               this.cachedText = displayFormat.replace("{score}", pointsStr).replace("{distance}", this.distance);
            } else {
               String displayFormat;
               if (this.baseText.startsWith("击败生物")) {
                  displayFormat = Config.killBonusDisplay;
               } else if (this.baseText.startsWith("助攻击败")) {
                  displayFormat = Config.assistBonusDisplay;
               } else if (this.baseText.startsWith("暴击加成")) {
                  displayFormat = Config.criticalBonusDisplay;
               } else if (this.baseText.startsWith("魔法伤害")) {
                  displayFormat = Config.magicBonusDisplay;
               } else if (this.baseText.startsWith("空手攻击")) {
                  displayFormat = Config.handBonusDisplay;
               } else {
                  if (!this.baseText.startsWith("造成伤害")) {
                     this.cachedText = this.baseText + pointsStr;
                     this.textDirty = false;
                     return this.cachedText;
                  }

                  displayFormat = Config.damageBonusDisplay;
               }

               this.cachedText = displayFormat.replace("{score}", pointsStr);
            }

            this.textDirty = false;
            return this.cachedText;
         }
      }

      public Text getCachedComponent() {
         if (this.cachedComponent == null || this.textDirty) {
            String formattedText = ScoreItemRenderer.convertToMinecraftFormat(this.getText());
            this.cachedComponent = Text.literal(formattedText);
            this.cachedTextWidth = -1;
            this.textDirty = false;
         }

         return this.cachedComponent;
      }

      public int getTextWidth(TextRenderer font) {
         if (this.cachedTextWidth == -1) {
            this.cachedTextWidth = font.getWidth(this.getCachedComponent());
         }

         return this.cachedTextWidth;
      }

      public void markTextDirty() {
         this.textDirty = true;
      }

      public void updateNumberAnimation(long currentTime) {
         boolean pointsChanged = false;
         if (Math.abs(this.currentPoints - this.targetPoints) > 0.01F) {
            long elapsed = currentTime - this.animationStartTime;
            if (elapsed >= 500L) {
               this.currentPoints = this.targetPoints;
            } else {
               float progress = (float)elapsed / 500.0F;
               float easedProgress = progress * progress;
               this.currentPoints = this.initialPoints + (this.targetPoints - this.initialPoints) * easedProgress;
            }

            pointsChanged = true;
         }

         if (pointsChanged) {
            this.markTextDirty();
         }

         if (this.isComboKill && this.isAnimatingCombo) {
            long elapsed = currentTime - KillIconRenderer.comboKillAnimationStart;
            if (elapsed >= 500L) {
               this.displayComboNumber = this.comboNumber;
               this.isAnimatingCombo = false;
               this.markTextDirty();
            } else {
               float progress = (float)elapsed / 500.0F;
               int steps = 11;
               int currentStep = (int)(progress * steps);
               this.displayComboNumber = (KillIconRenderer.lastComboCount + currentStep) % 10;
               if (this.displayComboNumber == 0) {
                  this.displayComboNumber = 10;
               }

               this.markTextDirty();
            }
         }
      }

      public void updateEntryAnimation(long currentTime) {
         if (this.isAnimating) {
            long animElapsed = currentTime - this.animationStartTime;
            float animProgress = Math.min((float)animElapsed / 75.0F, 1.0F);
            this.animationOffset = 8.0F * (1.0F - animProgress);
            if (animProgress >= 1.0F) {
               this.isAnimating = false;
               this.animationOffset = 0.0F;
            }
         }
      }
   }
}
