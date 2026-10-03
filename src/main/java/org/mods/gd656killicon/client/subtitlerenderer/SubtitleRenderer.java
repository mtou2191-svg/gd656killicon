package org.mods.gd656killicon.client.subtitlerenderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix3x2fStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.mods.gd656killicon.Config;
import org.mods.gd656killicon.client.KillIconRenderer;
import org.mods.gd656killicon.client.ModConfigScreen;
import org.mods.gd656killicon.client.RenderHelper;

@Environment(EnvType.CLIENT)
public class SubtitleRenderer {
   public void renderSubtitles(DrawContext guiGraphics, long currentTime) {
      if (RenderHelper.isElementVisible(ModConfigScreen.ElementType.SUBTITLE)) {
         if (!KillIconRenderer.latestWeaponName.isEmpty() || !KillIconRenderer.latestTargetName.isEmpty()) {
            if (KillIconRenderer.textHideTime <= 0L || currentTime < KillIconRenderer.textHideTime) {
               MinecraftClient mc = MinecraftClient.getInstance();
               TextRenderer font = mc.textRenderer;
               int screenWidth = mc.getWindow().getScaledWidth();
               int screenHeight = mc.getWindow().getScaledHeight();
               RenderHelper.PositionConfig pos = this.getCurrentPosition();
               String format = KillIconRenderer.latestIsAssist ? Config.assistSubtitleFormat : Config.customSubtitleFormat;
               String localizedWeapon = translateKey(KillIconRenderer.latestWeaponName);
               String localizedTarget = translateKey(KillIconRenderer.latestTargetName);
               String formattedText = format;
               if (format.contains("{weapon}") && !localizedWeapon.isEmpty()) {
                  formattedText = format.replace("{weapon}", localizedWeapon);
               }

               if (format.contains("{target}") && !localizedTarget.isEmpty()) {
                  formattedText = formattedText.replace("{target}", localizedTarget);
               }

               int weaponColorValue = KillIconRenderer.latestIsCritical ? Config.criticalColorHex : Config.weaponColorHex;
               int targetColorValue = KillIconRenderer.latestIsCritical ? Config.criticalColorHex : Config.targetColorHex;
               long elapsedSubtitle = currentTime - KillIconRenderer.subtitleAnimationStartTime;
               if (elapsedSubtitle < 250L) {
                  float progress = MathHelper.clamp((float)elapsedSubtitle / 250.0F, 0.0F, 1.0F);
                  float easedProgress = 1.0F - (float)Math.pow(1.0F - progress, 3.0);
                  KillIconRenderer.subtitleScale = MathHelper.lerp(easedProgress, 1.5F, 1.0F);
                  KillIconRenderer.subtitleBrightness = MathHelper.lerp(easedProgress, 10.0F, 1.0F);
               } else {
                  KillIconRenderer.subtitleScale = 1.0F;
                  KillIconRenderer.subtitleBrightness = 1.0F;
               }

               boolean hasWeaponPlaceholder = format.contains("{weapon}");
               boolean hasTargetPlaceholder = format.contains("{target}");
               Text fullText;
               if (hasWeaponPlaceholder && hasTargetPlaceholder) {
                  int weaponIndex = formattedText.indexOf(localizedWeapon);
                  int targetIndex = formattedText.indexOf(localizedTarget);
                  if (weaponIndex < 0 || targetIndex < 0) {
                     fullText = Text.literal(formattedText);
                  } else if (weaponIndex < targetIndex) {
                     String beforeWeapon = formattedText.substring(0, weaponIndex);
                     String between = formattedText.substring(Math.min(weaponIndex + localizedWeapon.length(), targetIndex), targetIndex);
                     String afterTarget = formattedText.substring(Math.min(targetIndex + localizedTarget.length(), formattedText.length()));
                     fullText = Text.literal(beforeWeapon)
                        .append(
                           Text.literal(localizedWeapon)
                              .fillStyle(
                                 Style.EMPTY.withColor(applyBrightness(weaponColorValue, KillIconRenderer.subtitleBrightness)).withBold(true)
                              )
                        )
                        .append(Text.literal(between))
                        .append(
                           Text.literal(localizedTarget)
                              .fillStyle(
                                 Style.EMPTY.withColor(applyBrightness(targetColorValue, KillIconRenderer.subtitleBrightness)).withBold(true)
                              )
                        )
                        .append(Text.literal(afterTarget));
                  } else {
                     String beforeTarget = formattedText.substring(0, targetIndex);
                     String between = formattedText.substring(Math.min(targetIndex + localizedTarget.length(), weaponIndex), weaponIndex);
                     String afterWeapon = formattedText.substring(Math.min(weaponIndex + localizedWeapon.length(), formattedText.length()));
                     fullText = Text.literal(beforeTarget)
                        .append(
                           Text.literal(localizedTarget)
                              .fillStyle(
                                 Style.EMPTY.withColor(applyBrightness(targetColorValue, KillIconRenderer.subtitleBrightness)).withBold(true)
                              )
                        )
                        .append(Text.literal(between))
                        .append(
                           Text.literal(localizedWeapon)
                              .fillStyle(
                                 Style.EMPTY.withColor(applyBrightness(weaponColorValue, KillIconRenderer.subtitleBrightness)).withBold(true)
                              )
                        )
                        .append(Text.literal(afterWeapon));
                  }
               } else if (hasWeaponPlaceholder) {
                  int weaponIndex = formattedText.indexOf(localizedWeapon);
                  if (weaponIndex >= 0) {
                     String beforeWeapon = formattedText.substring(0, weaponIndex);
                     String afterWeapon = formattedText.substring(Math.min(weaponIndex + localizedWeapon.length(), formattedText.length()));
                     fullText = Text.literal(beforeWeapon)
                        .append(
                           Text.literal(localizedWeapon)
                              .fillStyle(
                                 Style.EMPTY.withColor(applyBrightness(weaponColorValue, KillIconRenderer.subtitleBrightness)).withBold(true)
                              )
                        )
                        .append(Text.literal(afterWeapon));
                  } else {
                     fullText = Text.literal(formattedText);
                  }
               } else if (hasTargetPlaceholder) {
                  int targetIndex = formattedText.indexOf(localizedTarget);
                  if (targetIndex >= 0) {
                     String beforeTarget = formattedText.substring(0, targetIndex);
                     String afterTarget = formattedText.substring(Math.min(targetIndex + localizedTarget.length(), formattedText.length()));
                     fullText = Text.literal(beforeTarget)
                        .append(
                           Text.literal(localizedTarget)
                              .fillStyle(
                                 Style.EMPTY.withColor(applyBrightness(targetColorValue, KillIconRenderer.subtitleBrightness)).withBold(true)
                              )
                        )
                        .append(Text.literal(afterTarget));
                  } else {
                     fullText = Text.literal(formattedText);
                  }
               } else {
                  fullText = Text.literal(formattedText);
               }

               int textY = screenHeight + pos.subtitleY;
               int textWidth = font.getWidth(fullText);
               int textX = screenWidth / 2 + pos.subtitleX - textWidth / 2;
               float subtitleSizeScale = RenderHelper.getElementSize(ModConfigScreen.ElementType.SUBTITLE) / 100.0F;
               Matrix3x2fStack poseStack = guiGraphics.getMatrices();
               poseStack.pushMatrix();

               try {
                  float centerX = textX + textWidth / 2.0F;
                  float centerY = textY + 9.0F / 2.0F;
                  poseStack.translate(centerX, centerY);
                  poseStack.scale(KillIconRenderer.subtitleScale * subtitleSizeScale, KillIconRenderer.subtitleScale * subtitleSizeScale);
                  poseStack.translate(-centerX, -centerY);
                  guiGraphics.drawText(font, fullText, textX, textY, applyBrightness(-1, KillIconRenderer.subtitleBrightness), true);
               } finally {
                  poseStack.popMatrix();
               }
            }
         }
      }
   }

   private static int applyBrightness(int color, float brightness) {
      int a = color >> 24 & 0xFF;
      int r = (int)((color >> 16 & 0xFF) * brightness);
      int g = (int)((color >> 8 & 0xFF) * brightness);
      int b = (int)((color & 0xFF) * brightness);
      r = MathHelper.clamp(r, 0, 255);
      g = MathHelper.clamp(g, 0, 255);
      b = MathHelper.clamp(b, 0, 255);
      return a << 24 | r << 16 | g << 8 | b;
   }

   private static String translateKey(String key) {
      try {
         Text translated = Text.translatable(key);
         return translated.getString();
      } catch (Exception var2) {
         return key.contains(".") ? key.substring(key.lastIndexOf(".") + 1) : key;
      }
   }

   private RenderHelper.PositionConfig getCurrentPosition() {
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
}
