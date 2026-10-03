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
public class CardModeRenderer {
   private static int killChainTimeout = 60000;
   private static long bottomBarFlashStartTime = 0L;
   private static boolean bottomBarFlashing = false;

   public static void setKillChainTimeout(int timeout) {
      killChainTimeout = timeout;
   }

   public static int getKillChainTimeout() {
      return killChainTimeout;
   }

   public void handleCardMode() {
      MinecraftClient mc = MinecraftClient.getInstance();
      int screenWidth = mc.getWindow().getScaledWidth();
      int screenHeight = mc.getWindow().getScaledHeight();
      getCurrentPosition();
      float startX = screenWidth / 2.0F + Config.cardIconX;
      float startY = screenHeight + Config.cardIconY;
      long currentTime = System.currentTimeMillis();
      KillIconRenderer.lastKillTime = currentTime;
      if (Config.cardBottombarVisible) {
         bottomBarFlashStartTime = currentTime;
         bottomBarFlashing = true;
      }

      for (CardModeRenderer.CardInstance card : KillIconRenderer.activeCards) {
         if (card.lightVisible) {
            this.startLightRemoveAnimation(card, currentTime);
         }
      }

      KillIconRenderer.cardComboCount++;
      if (KillIconRenderer.cardComboCount == 6) {
         this.startAceFormationAnimation(currentTime);
      } else if (KillIconRenderer.cardComboCount >= 7) {
         this.handleAceCardMode(currentTime);
      } else {
         CardModeRenderer.CardInstance newCard = this.createCardInstance(startX, startY, currentTime);
         KillIconRenderer.activeCards.add(newCard);
         this.calculateCardPositions(KillIconRenderer.cardComboCount);
      }

      if (Config.enableSoundEffects) {
         float volume = Config.soundVolume / 100.0F;
         SoundEvent sound = getCardKillSound();
         Gd656killiconClient.playSoundWithCooldown(sound, volume);
      }

      KillIconRenderer.latestWeaponName = "";
      KillIconRenderer.latestTargetName = "";
      KillIconRenderer.textAnimationStartTime = 0L;
   }

   public void renderCards(DrawContext guiGraphics, Matrix3x2fStack poseStack, long currentTime) {
      KillIconRenderer.resetComboIfNeeded();
      if (!KillIconRenderer.activeCards.isEmpty()) {
         boolean chainEnded = false;
         if (killChainTimeout > 0) {
            chainEnded = currentTime - KillIconRenderer.lastKillTime > killChainTimeout;
         }

         if (chainEnded) {
            long fadeElapsed = currentTime - KillIconRenderer.lastKillTime - killChainTimeout;
            float fadeProgress = MathHelper.clamp((float)fadeElapsed / 1000.0F, 0.0F, 1.0F);

            for (CardModeRenderer.CardInstance card : KillIconRenderer.activeCards) {
               card.alpha = 1.0F - fadeProgress;
            }

            if (fadeProgress >= 1.0F) {
               KillIconRenderer.activeCards.clear();
               return;
            }
         } else {
            for (CardModeRenderer.CardInstance card : KillIconRenderer.activeCards) {
               if (card.alpha < 1.0F) {
                  card.alpha = 1.0F;
               }
            }
         }

         for (CardModeRenderer.CardInstance cardx : KillIconRenderer.activeCards) {
            long elapsed = currentTime - cardx.startTime;
            updateCardAnimation(cardx, elapsed, currentTime);
            if (cardx.lightVisible && cardx.lightAlpha > 0.0F) {
               renderLightEffect(guiGraphics, poseStack, cardx);
            }
         }

         for (CardModeRenderer.CardInstance cardxx : KillIconRenderer.activeCards) {
            renderSingleCard(guiGraphics, poseStack, cardxx);
         }
      }
   }

   public static void renderBottomBar(DrawContext guiGraphics, Matrix3x2fStack poseStack) {
      if (Config.cardBottombarVisible) {
         float brightness = 1.0F;
         if (bottomBarFlashing) {
            long currentTime = System.currentTimeMillis();
            long elapsed = currentTime - bottomBarFlashStartTime;
            if (elapsed < 100L) {
               float progress = MathHelper.clamp((float)elapsed / 100.0F, 0.0F, 1.0F);
               brightness = 1.0F + 1.5F * progress;
            } else if (elapsed < 200L) {
               float progress = MathHelper.clamp((float)(elapsed - 100L) / 100.0F, 0.0F, 1.0F);
               brightness = 2.5F - 1.5F * progress;
            } else {
               bottomBarFlashing = false;
            }
         }

         poseStack.pushMatrix();
         MinecraftClient mc = MinecraftClient.getInstance();
         int screenWidth = mc.getWindow().getScaledWidth();
         int screenHeight = mc.getWindow().getScaledHeight();
         RenderHelper.PositionConfig pos = getCurrentPosition();
         float centerX = screenWidth / 2.0F + pos.iconX;
         float originY = screenHeight + pos.iconY + Config.cardBottombarOffsetY;
         float scale = Config.cardBottombarSize / 100.0F * 0.25F;
         int width = (int)(256.0F * scale);
         int height = (int)(256.0F * scale);
         float renderX = centerX - width / 2.0F;
         float renderY = originY - height / 2.0F;
         Identifier bottomBarTexture = getBottomBarTexture();
         int alpha = (int)(0.9F * 255.0F) & 255;
         int bright = (int)MathHelper.clamp(brightness * 255.0F, 0.0F, 255.0F) & 255;
         int color = alpha << 24 | bright << 16 | bright << 8 | bright;
         guiGraphics.drawTexture(
            RenderPipelines.GUI_TEXTURED, bottomBarTexture, (int)renderX, (int)renderY, 0.0F, 0.0F, width, height, 2400, 256, 2400, 256, color
         );
         poseStack.popMatrix();
      }
   }

   private void handleAceCardMode(long currentTime) {
      if (KillIconRenderer.cardComboCount == 6) {
         this.startAceFormationAnimation(currentTime);
      } else {
         this.handleNormalAceCard(currentTime);
      }

      if (Config.enableSoundEffects) {
         float volume = Config.soundVolume / 100.0F;
         SoundEvent sound = getCardKillSound();
         Gd656killiconClient.playSoundWithCooldown(sound, volume);
      }

      KillIconRenderer.latestWeaponName = "";
      KillIconRenderer.latestTargetName = "";
      KillIconRenderer.textAnimationStartTime = 0L;
   }

   private void calculateCardPositions(int comboCount) {
      MinecraftClient mc = MinecraftClient.getInstance();
      int screenWidth = mc.getWindow().getScaledWidth();
      int screenHeight = mc.getWindow().getScaledHeight();
      RenderHelper.PositionConfig pos = getCurrentPosition();
      float centerX = screenWidth / 2.0F + pos.iconX;
      float targetBaseY = screenHeight + pos.iconY;
      comboCount = Math.min(comboCount, 5);

      for (CardModeRenderer.CardInstance card : KillIconRenderer.activeCards) {
         card.startX = card.x;
         card.startY = card.y;
         card.startRotation = card.rotation;
         card.animationStartTime = System.currentTimeMillis();
         card.isAnimating = true;
         card.lightOffsetX = 0.0F;
         card.lightOffsetY = 0.0F;
         card.lightRotation = 0.0F;
         switch (comboCount) {
            case 1:
               card.targetX = centerX;
               card.targetY = targetBaseY;
               card.targetRotation = 0.0F;
               break;
            case 2:
               if (card.comboPosition == 0) {
                  card.targetX = centerX - 10.0F;
                  card.targetY = targetBaseY;
                  card.targetRotation = -5.0F;
                  card.lightOffsetX = -10.0F;
                  card.lightRotation = -5.0F;
               } else {
                  card.targetX = centerX + 10.0F;
                  card.targetY = targetBaseY;
                  card.targetRotation = 5.0F;
                  card.lightOffsetX = 1.25F;
                  card.lightRotation = 5.0F;
               }
               break;
            case 3:
               if (card.comboPosition == 0) {
                  card.targetX = centerX - 15.0F;
                  card.targetY = targetBaseY;
                  card.targetRotation = -10.0F;
                  card.lightOffsetX = -15.0F;
                  card.lightRotation = -10.0F;
               } else if (card.comboPosition == 1) {
                  card.targetX = centerX;
                  card.targetY = targetBaseY;
                  card.targetRotation = 0.0F;
               } else {
                  card.targetX = centerX + 15.0F;
                  card.targetY = targetBaseY;
                  card.targetRotation = 10.0F;
                  card.lightOffsetX = 2.25F;
                  card.lightRotation = 10.0F;
               }
               break;
            case 4:
               if (card.comboPosition == 0) {
                  card.targetX = centerX - 15.0F;
                  card.targetY = targetBaseY;
                  card.targetRotation = -15.0F;
                  card.lightOffsetX = -15.0F;
                  card.lightRotation = -15.0F;
               } else if (card.comboPosition == 1) {
                  card.targetX = centerX - 5.0F;
                  card.targetY = targetBaseY;
                  card.targetRotation = -5.0F;
                  card.lightOffsetX = -5.0F;
                  card.lightRotation = -5.0F;
               } else if (card.comboPosition == 2) {
                  card.targetX = centerX + 5.0F;
                  card.targetY = targetBaseY;
                  card.targetRotation = 5.0F;
                  card.lightOffsetX = 5.0F;
                  card.lightRotation = 5.0F;
               } else {
                  card.targetX = centerX + 15.0F;
                  card.targetY = targetBaseY;
                  card.targetRotation = 15.0F;
                  card.lightOffsetX = 2.75F;
                  card.lightRotation = 15.0F;
               }
               break;
            case 5:
               if (card.comboPosition == 0) {
                  card.targetX = centerX - 20.0F;
                  card.targetY = targetBaseY + 3.0F;
                  card.targetRotation = -30.0F;
                  card.lightOffsetX = -20.0F;
                  card.lightOffsetY = 3.0F;
                  card.lightRotation = -30.0F;
               } else if (card.comboPosition == 1) {
                  card.targetX = centerX - 10.0F;
                  card.targetY = targetBaseY;
                  card.targetRotation = -15.0F;
                  card.lightOffsetX = -10.0F;
                  card.lightRotation = -15.0F;
               } else if (card.comboPosition == 2) {
                  card.targetX = centerX;
                  card.targetY = targetBaseY;
                  card.targetRotation = 0.0F;
               } else if (card.comboPosition == 3) {
                  card.targetX = centerX + 10.0F;
                  card.targetY = targetBaseY;
                  card.targetRotation = 15.0F;
                  card.lightOffsetX = 10.0F;
                  card.lightRotation = 15.0F;
               } else {
                  card.targetX = centerX + 20.0F;
                  card.targetY = targetBaseY + 3.0F;
                  card.targetRotation = 30.0F;
                  card.lightOffsetX = 3.0F;
                  card.lightOffsetY = 3.0F;
                  card.lightRotation = 30.0F;
               }
         }

         card.lightTargetX = card.targetX;
         card.lightTargetY = card.targetY;
      }
   }

   private CardModeRenderer.CardInstance createCardInstance(float startX, float startY, long currentTime) {
      int cardWidth = 48;
      int cardHeight = 72;
      int cardTypeIndex = KillIconRenderer.cardComboCount - 1;
      CardModeRenderer.CardInstance card = new CardModeRenderer.CardInstance(
         currentTime, startX, startY, cardTypeIndex, cardWidth, cardHeight, KillIconRenderer.cardComboCount - 1
      );
      RenderHelper.PositionConfig pos = getCurrentPosition();
      card.targetX = MinecraftClient.getInstance().getWindow().getScaledWidth() / 2.0F + pos.iconX;
      card.targetY = MinecraftClient.getInstance().getWindow().getScaledHeight() + pos.iconY;
      updateCardLightTexture(card);
      return card;
   }

   private void handleNormalAceCard(long currentTime) {
      MinecraftClient mc = MinecraftClient.getInstance();
      int screenWidth = mc.getWindow().getScaledWidth();
      int screenHeight = mc.getWindow().getScaledHeight();
      RenderHelper.PositionConfig pos = getCurrentPosition();
      float originX = screenWidth / 2.0F + pos.iconX;
      float originY = screenHeight + pos.iconY;
      int cardWidth = 48;
      int cardHeight = 72;
      CardModeRenderer.CardInstance aceCard = new CardModeRenderer.CardInstance(currentTime, originX, originY, 4, cardWidth, cardHeight, 0);
      aceCard.isAceCard = true;
      aceCard.scale = 0.75F;
      aceCard.brightness = 1024.0F;
      aceCard.isAnimating = true;
      aceCard.animationStartTime = currentTime;
      aceCard.targetX = originX;
      aceCard.targetY = originY;
      aceCard.targetRotation = 0.0F;
      aceCard.startX = originX;
      aceCard.startY = originY;
      aceCard.startRotation = 0.0F;
      aceCard.lightOffsetX = 0.0F;
      aceCard.lightOffsetY = 0.0F;
      aceCard.lightRotation = 0.0F;
      updateCardLightTexture(aceCard);
      KillIconRenderer.activeCards.add(aceCard);
   }

   private void startAceFormationAnimation(long currentTime) {
      RenderHelper.PositionConfig pos = getCurrentPosition();
      MinecraftClient mc = MinecraftClient.getInstance();
      int screenWidth = mc.getWindow().getScaledWidth();
      int screenHeight = mc.getWindow().getScaledHeight();
      float centerX = screenWidth / 2.0F + pos.iconX;
      float centerY = screenHeight + pos.iconY;
      KillIconRenderer.activeCards.removeIf(card -> card.isAceCard);

      for (CardModeRenderer.CardInstance card : KillIconRenderer.activeCards) {
         card.isFormingAce = true;
         card.targetX = centerX;
         card.targetY = centerY;
         card.targetRotation = 0.0F;
         card.animationStartTime = currentTime;
         card.isAnimating = true;
         card.lightOffsetX = 0.0F;
         card.lightOffsetY = 0.0F;
         card.lightRotation = 0.0F;
         if (card.cardIndex != 4) {
            card.shouldRemoveAfterAce = true;
         }
      }

      boolean hasAceCard = false;

      for (CardModeRenderer.CardInstance cardx : KillIconRenderer.activeCards) {
         if (cardx.cardIndex == 4) {
            hasAceCard = true;
            cardx.isAceCard = true;
            cardx.alpha = 0.0F;
            cardx.lightStartTime = currentTime;
            cardx.lightVisible = true;
            cardx.lightAlpha = 0.0F;
            cardx.lightBrightness = 1.0F;
            cardx.lightYOffset = 200;
            cardx.lightScale = 1.0F;
            break;
         }
      }

      if (!hasAceCard) {
         int cardWidth = 48;
         int cardHeight = 72;
         CardModeRenderer.CardInstance aceCard = new CardModeRenderer.CardInstance(currentTime, centerX, centerY, 4, cardWidth, cardHeight, 4);
         aceCard.isAceCard = true;
         aceCard.alpha = 0.0F;
         aceCard.isFormingAce = true;
         aceCard.lightStartTime = currentTime;
         aceCard.lightVisible = true;
         aceCard.lightAlpha = 0.0F;
         aceCard.lightBrightness = 1.0F;
         aceCard.lightYOffset = 200;
         aceCard.lightScale = 1.0F;
         KillIconRenderer.activeCards.add(aceCard);
      }
   }

   private static void renderSingleCard(DrawContext guiGraphics, Matrix3x2fStack poseStack, CardModeRenderer.CardInstance card) {
      float finalScale = card.scale;
      float finalBrightness = card.brightness;
      poseStack.pushMatrix();
      float centerX = card.x - card.cardWidth * finalScale / 2.0F;
      float centerY = card.y - card.cardHeight * finalScale / 2.0F;
      poseStack.translate(centerX + card.cardWidth * finalScale / 2.0F, centerY + card.cardHeight * finalScale / 2.0F);
      poseStack.rotate((float)Math.toRadians(card.rotation));
      poseStack.translate(-(card.cardWidth * finalScale) / 2.0F, -(card.cardHeight * finalScale) / 2.0F);
      poseStack.scale(finalScale, finalScale);
      Identifier cardTexture = getCardTexture(card.cardIndex);
      int alpha = (int)(card.alpha * 255.0F) & 255;
      int bright = (int)MathHelper.clamp(finalBrightness * 255.0F, 0.0F, 255.0F) & 255;
      int color = alpha << 24 | bright << 16 | bright << 8 | bright;
      guiGraphics.drawTexture(
         RenderPipelines.GUI_TEXTURED, cardTexture, 0, 0, 0.0F, 0.0F, card.cardWidth, card.cardHeight, card.cardWidth, card.cardHeight, card.cardWidth, card.cardHeight, color
      );
      poseStack.popMatrix();
   }

   private static void renderLightEffect(DrawContext guiGraphics, Matrix3x2fStack poseStack, CardModeRenderer.CardInstance card) {
      if (card.lightTexture != null && !(card.lightAlpha <= 0.0F)) {
         poseStack.pushMatrix();
         float scaleToCardWidth = card.cardWidth * card.scale / 248.0F;
         float baseScale = card.lightScale;
         float scaledLightWidth = 248.0F * scaleToCardWidth * baseScale;
         float scaledLightHeight = 2048.0F * scaleToCardWidth * baseScale;
         float lightX = card.x + card.lightOffsetX - scaledLightWidth / 2.0F;
         float lightY = card.y + card.lightOffsetY + card.lightYOffset - scaledLightHeight / 2.0F;
         poseStack.translate(lightX, lightY);
         if (card.lightRotation != 0.0F) {
            poseStack.translate(scaledLightWidth / 2.0F, scaledLightHeight / 2.0F);
            poseStack.rotate((float)Math.toRadians(card.lightRotation));
            poseStack.translate(-scaledLightWidth / 2.0F, -scaledLightHeight / 2.0F);
         }

         int alpha = (int)(card.lightAlpha * 255.0F) & 255;
         int bright = (int)MathHelper.clamp(card.lightBrightness * 255.0F, 0.0F, 255.0F) & 255;
         int color = alpha << 24 | bright << 16 | bright << 8 | bright;
         guiGraphics.drawTexture(
            RenderPipelines.GUI_TEXTURED,
            card.lightTexture,
            0,
            0,
            0.0F,
            0.0F,
            (int)scaledLightWidth,
            (int)scaledLightHeight,
            248,
            2048,
            248,
            2048,
            color
         );
         poseStack.popMatrix();
      }
   }

   private void startLightRemoveAnimation(CardModeRenderer.CardInstance card, long currentTime) {
      if (card.lightVisible) {
         long elapsedInCurrentPhase = currentTime - card.lightStartTime;
         if (elapsedInCurrentPhase < 500L) {
            card.lightStartTime = currentTime - 500L;
         } else if (elapsedInCurrentPhase < 3000L) {
            long remainingFadeTime = 3000L - elapsedInCurrentPhase;
            card.lightStartTime = currentTime - (3000L - Math.min(remainingFadeTime, 500L));
         }
      }
   }

   private static void updateCardAnimation(CardModeRenderer.CardInstance card, long elapsed, long currentTime) {
      float elapsedSeconds = (float)elapsed / 1000.0F;
      long animElapsed = currentTime - card.animationStartTime;
      float animProgress = MathHelper.clamp((float)animElapsed / 500.0F, 0.0F, 1.0F);
      float easedProgress = 1.0F - (float)Math.pow(1.0F - animProgress, 3.0);
      if (card.isFormingAce) {
         card.x = MathHelper.lerp(easedProgress, card.startX, card.targetX);
         card.y = MathHelper.lerp(easedProgress, card.startY, card.targetY);
         card.rotation = MathHelper.lerp(easedProgress, card.startRotation, card.targetRotation);
         if (card.isAceCard) {
            card.alpha = MathHelper.clamp(animProgress * 2.0F, 0.0F, 1.0F);
         }

         if (animProgress >= 1.0F) {
            card.isFormingAce = false;
            if (card.shouldRemoveAfterAce) {
               card.alpha = 0.0F;
            }
         }
      } else if (card.isAceCard && KillIconRenderer.cardComboCount >= 7) {
         if (card.isAnimating) {
            card.x = MathHelper.lerp(easedProgress, card.startX, card.targetX);
            card.y = MathHelper.lerp(easedProgress, card.startY, card.targetY);
            card.rotation = MathHelper.lerp(easedProgress, card.startRotation, card.targetRotation);
            if (animProgress >= 1.0F) {
               card.isAnimating = false;
            }
         }
      } else if (card.isAnimating) {
         card.x = MathHelper.lerp(easedProgress, card.startX, card.targetX);
         card.y = MathHelper.lerp(easedProgress, card.startY, card.targetY);
         card.rotation = MathHelper.lerp(easedProgress, card.startRotation, card.targetRotation);
         if (animProgress >= 1.0F) {
            card.isAnimating = false;
         }
      }

      if (elapsedSeconds < 0.5F) {
         card.brightness = 2.0F;
      } else if (elapsedSeconds < 0.8F) {
         float progress = MathHelper.clamp((elapsedSeconds - 0.5F) / 0.3F, 0.0F, 1.0F);
         card.brightness = 2.0F * (1.0F - progress) + progress;
      } else {
         card.brightness = 1.0F;
      }

      updateLightAnimation(card, currentTime);
   }

   private static void updateLightAnimation(CardModeRenderer.CardInstance card, long currentTime) {
      if (card.lightVisible) {
         long lightElapsed = currentTime - card.lightStartTime;
         if (lightElapsed < 500L) {
            float progress = MathHelper.clamp((float)lightElapsed / 500.0F, 0.0F, 1.0F);
            float easedProgress = easeOutBack(progress);
            card.lightYOffset = (int)MathHelper.lerp(easedProgress, 200.0F, 0.0F);
            if (lightElapsed < 100L) {
               float flashProgress = MathHelper.clamp((float)lightElapsed / 100.0F, 0.0F, 1.0F);
               float easedFlash = flashProgress * flashProgress;
               card.lightBrightness = MathHelper.lerp(easedFlash, 1.0F, 2.5F);
            } else if (lightElapsed < 200L) {
               float flashProgress = MathHelper.clamp((float)(lightElapsed - 100L) / 100.0F, 0.0F, 1.0F);
               float easedFlash = flashProgress * flashProgress;
               card.lightBrightness = MathHelper.lerp(easedFlash, 2.5F, 1.0F);
            } else {
               card.lightBrightness = 1.0F;
            }

            card.lightAlpha = easedProgress;
            card.lightScale = 1.0F;
         } else if (lightElapsed < 3000L) {
            card.lightYOffset = 0;
            card.lightBrightness = 1.0F;
            float scaleElapsed = (float)(lightElapsed - 500L);
            float scaleProgress = MathHelper.clamp(scaleElapsed / 3000.0F, 0.0F, 1.0F);
            float easedScaleProgress = 1.0F - (float)Math.pow(1.0F - scaleProgress, 2.0);
            card.lightScale = 1.0F - easedScaleProgress * 0.2F;
            float fadeProgress = MathHelper.clamp(scaleElapsed / 2500.0F, 0.0F, 1.0F);
            float easedFadeProgress = 1.0F - (float)Math.pow(1.0F - fadeProgress, 3.0);
            card.lightAlpha = 1.0F - easedFadeProgress;
         } else {
            card.lightAlpha = 0.0F;
            card.lightVisible = false;
         }

         updateLightPosition(card);
      }
   }

   private static float easeOutBack(float x) {
      float c1 = 1.70158F;
      float c3 = c1 + 1.0F;
      return 1.0F + c3 * (float)Math.pow(x - 1.0F, 3.0) + c1 * (float)Math.pow(x - 1.0F, 2.0);
   }

   private static SoundEvent getCardKillSound() {
      return Gd656killicon.getCardKillSound();
   }

   private static Identifier getCardTexture(int cardIndex) {
      String stylePath = Config.cardIconStyle == Config.IconStyle.VANILLA ? "vanilla_gui" : "modern_gui";
      String cardFileName = cardIndex < 4 ? "killcard_" + (cardIndex + 1) + ".png" : "killcard_ace.png";
      return Identifier.of("gd656killicon", "textures/gui/" + stylePath + "/cardmode/" + cardFileName);
   }

   private static Identifier getBottomBarTexture() {
      String stylePath = Config.cardIconStyle == Config.IconStyle.VANILLA ? "vanilla_gui" : "modern_gui";
      return Identifier.of("gd656killicon", "textures/gui/" + stylePath + "/cardmode/bottom_bar.png");
   }

   private static RenderHelper.PositionConfig getCurrentPosition() {
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

   private static void updateCardLightTexture(CardModeRenderer.CardInstance card) {
      String stylePath = Config.cardIconStyle == Config.IconStyle.VANILLA ? "vanilla_gui" : "modern_gui";
      card.lightTexture = Identifier.of("gd656killicon", "textures/gui/" + stylePath + "/cardmode/light.png");
   }

   private static void updateLightPosition(CardModeRenderer.CardInstance card) {
      card.lightX = card.x;
      float cardBottomY = card.y + card.cardHeight * card.scale / 2.0F;
      card.lightY = cardBottomY - card.lightOffsetY;
   }

   public static class CardInstance {
      public float alpha;
      public long animationStartTime;
      public float brightness;
      public int cardHeight;
      public int cardIndex;
      public int cardWidth;
      public int comboPosition;
      public boolean isAceCard = false;
      public boolean isAnimating;
      public boolean isFormingAce = false;
      public float rotation;
      public float scale;
      public boolean shouldRemoveAfterAce = false;
      public long startTime;
      public float startRotation;
      public float startX;
      public float startY;
      public float targetRotation;
      public float targetX;
      public float targetY;
      public float x;
      public float y;
      public float lightAlpha;
      public float lightBrightness;
      public float lightOffsetX = 0.0F;
      public float lightOffsetY;
      public float lightRotation = 0.0F;
      public float lightScale;
      public long lightStartTime;
      public Identifier lightTexture;
      public float lightTargetX;
      public float lightTargetY;
      public boolean lightVisible;
      public float lightX;
      public float lightY;
      private int lightYOffset;

      public CardInstance(long startTime, float x, float y, int cardIndex, int width, int height, int comboPosition) {
         this.startTime = startTime;
         this.x = x;
         this.y = y;
         this.startX = x;
         this.startY = y;
         this.targetX = x;
         this.targetY = y - 30.0F;
         this.brightness = 1024.0F;
         this.scale = 0.75F;
         this.alpha = 1.0F;
         this.cardIndex = cardIndex;
         this.cardWidth = width;
         this.cardHeight = height;
         this.rotation = 0.0F;
         this.targetRotation = 0.0F;
         this.startRotation = 0.0F;
         this.animationStartTime = startTime;
         this.isAnimating = true;
         this.comboPosition = comboPosition;
         this.lightStartTime = startTime;
         this.lightVisible = true;
         this.lightAlpha = 0.0F;
         this.lightBrightness = 1.0F;
         this.lightOffsetY = height * 2;
         this.lightYOffset = 200;
         this.lightScale = 1.0F;
         this.updateLightTexture();
      }

      private void updateLightTexture() {
         String stylePath = Config.cardIconStyle == Config.IconStyle.VANILLA ? "vanilla_gui" : "modern_gui";
         this.lightTexture = Identifier.of("gd656killicon", "textures/gui/" + stylePath + "/cardmode/light.png");
      }
   }
}
