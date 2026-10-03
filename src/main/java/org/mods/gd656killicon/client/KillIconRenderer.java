package org.mods.gd656killicon.client;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import org.joml.Matrix3x2fStack;
import org.mods.gd656killicon.Config;
import org.mods.gd656killicon.Gd656killiconClient;
import org.mods.gd656killicon.client.configmenu.HistoryRecordScreen;
import org.mods.gd656killicon.client.iconrenderer.CardModeRenderer;
import org.mods.gd656killicon.client.iconrenderer.ComboIconRenderer;
import org.mods.gd656killicon.client.iconrenderer.ScrollingIconRenderer;
import org.mods.gd656killicon.client.subtitlerenderer.ComboScoreRenderer;
import org.mods.gd656killicon.client.subtitlerenderer.ScoreItemRenderer;
import org.mods.gd656killicon.client.subtitlerenderer.SubtitleRenderer;
import org.mods.gd656killicon.util.ClientUtil;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class KillIconRenderer implements HudRenderCallback {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final List<ScrollingIconRenderer.KillIconInstance> activeIcons = new ArrayList<>();
   public static final List<CardModeRenderer.CardInstance> activeCards = new ArrayList<>();
   public static final List<ScoreItemRenderer.ScoreItem> activeScoreItems = new ArrayList<>();
   public static final Queue<ScoreItemRenderer.ScoreItem> scoreItems = new LinkedList<>();
   public static Config.IconStyle currentIconStyle = null;
   public static Config.IconStyle currentCardIconStyle = null;
   public static net.minecraft.util.Identifier[] currentCardIcons;
   public static String latestWeaponName = "";
   public static String latestTargetName = "";
   public static boolean latestIsCritical = false;
   public static boolean latestIsAssist = false;
   public static boolean DEBUG = false;
   public static int comboCount = 0;
   public static int lastComboCount = 0;
   public static int comboScore = 0;
   public static int displayedComboScore = 0;
   public static int targetComboScore = 0;
   public static int cardComboCount = 0;
   public static int longRangeDistance = 0;
   public static int longRangeBonus = 0;
   public static int scoreAnimationStart = 0;
   public static int flashCount = 0;
   public static long comboKillAnimationStart = 0L;
   public static long textAnimationStartTime = 0L;
   public static long textHideTime = 0L;
   public static long lastKillTime = 0L;
   public static long lastComboTime = 0L;
   public static long lastScoreUpdateTime = 0L;
   public static long comboScoreShowTime = 0L;
   public static long lastDamageTime = 0L;
   public static long lastFlashTime = 0L;
   public static long longRangeShowTime = 0L;
   public static long groupDisplayStartTime = 0L;
   public static long renderTimeSum = 0L;
   public static long subtitleAnimationStartTime = 0L;
   public static boolean isFadingOutAll = false;
   public static boolean isComboScoreVisible = false;
   public static boolean isFlashing = false;
   public static boolean isGroupDisplaying = false;
   public static boolean shouldStartFadeOut = false;
   public static float subtitleBrightness = 1.0F;
   public static float subtitleScale = 1.0F;
   public static ScoreItemRenderer.ScoreItem comboKillItem = null;
   private static boolean isRendering = false;
   private static final int KILL_CHAIN_TIMEOUT = 3000;
   private static final int COMBO_KILL_BONUS_MULTIPLIER = 8;
   private static final long LONG_RANGE_DISPLAY_DURATION = 3000L;
   private static int renderCount = 0;
   static ScrollingIconRenderer scrollingRenderer = new ScrollingIconRenderer();
   static ComboIconRenderer comboRenderer = new ComboIconRenderer();
   private static final CardModeRenderer cardRenderer = new CardModeRenderer();
   private static final SubtitleRenderer subtitleRenderer = new SubtitleRenderer();
   private static final ComboScoreRenderer comboScoreRenderer = new ComboScoreRenderer();
   private static final RenderHelper renderHelper = new RenderHelper();

   public void onHudRender(DrawContext guiGraphics, RenderTickCounter tickCounter) {
      long startTime = System.nanoTime();
      if (MinecraftClient.getInstance().currentScreen == null) {
         if (currentIconStyle != Config.iconStyle || currentCardIconStyle != Config.cardIconStyle) {
            renderHelper.loadResources();
            ComboIconRenderer.loadResources();
         }

         if (ClientUtil.isClient()) {
            if (!isRendering) {
               isRendering = true;

               try {
                  Gd656killiconClient.cleanupExpiredSoundCooldowns();
                  if (!Config.showKillIcons && !Config.showKillSubtitles && !Config.showComboScore) {
                     return;
                  }

                  Matrix3x2fStack poseStack = guiGraphics.getMatrices();
                  long currentTime = System.currentTimeMillis();
                  updateGroupDisplayTimer(currentTime);
                  if (Config.iconMode == Config.IconMode.CARD) {
                     cardRenderer.renderCards(guiGraphics, poseStack, currentTime);
                     if (RenderHelper.isElementVisible(ModConfigScreen.ElementType.SUBTITLE)) {
                        subtitleRenderer.renderSubtitles(guiGraphics, currentTime);
                     }

                     if (RenderHelper.isElementVisible(ModConfigScreen.ElementType.SCORE)) {
                        comboScoreRenderer.renderComboScore(guiGraphics, currentTime);
                        renderLongRangeBonus(guiGraphics, currentTime);
                     }

                     if (Config.cardBottombarVisible) {
                        CardModeRenderer.renderBottomBar(guiGraphics, poseStack);
                     }

                     if (Config.debugShowInfo) {
                        addDebugInfo(guiGraphics);
                     }

                     return;
                  }

                  boolean chainEnded = currentTime - lastKillTime > 3000L;
                  if (chainEnded && !isGroupDisplaying) {
                     groupDisplayStartTime = currentTime;
                     isGroupDisplaying = true;
                  }

                  if (activeIcons.isEmpty()) {
                     latestWeaponName = "";
                     latestTargetName = "";
                     latestIsAssist = false;
                     textAnimationStartTime = 0L;
                  } else if (textAnimationStartTime > 0L) {
                     long displayTime = Config.killIconDuration * 80L;
                     if (currentTime - textAnimationStartTime > displayTime && textHideTime == 0L) {
                        textHideTime = currentTime + 500L;
                     }

                     if (textHideTime > 0L && currentTime >= textHideTime) {
                        latestWeaponName = "";
                        latestTargetName = "";
                        latestIsAssist = false;
                        textAnimationStartTime = 0L;
                        textHideTime = 0L;
                     }
                  }

                  if (RenderHelper.isElementVisible(ModConfigScreen.ElementType.ICON)) {
                     if (Config.iconMode == Config.IconMode.SCROLLING) {
                        scrollingRenderer.renderIcons(guiGraphics, poseStack, currentTime);
                     } else if (Config.iconMode == Config.IconMode.COMBO) {
                        scrollingRenderer.renderIcons(guiGraphics, poseStack, currentTime);
                     }
                  }

                  if (RenderHelper.isElementVisible(ModConfigScreen.ElementType.SUBTITLE)) {
                     subtitleRenderer.renderSubtitles(guiGraphics, currentTime);
                  }

                  if (RenderHelper.isElementVisible(ModConfigScreen.ElementType.SCORE)) {
                     comboScoreRenderer.renderComboScore(guiGraphics, currentTime);
                     renderLongRangeBonus(guiGraphics, currentTime);
                  }

                  if (Config.debugShowInfo) {
                     addDebugInfo(guiGraphics);
                  }
               } finally {
                  isRendering = false;
               }

               long endTime = System.nanoTime();
               long renderTime = endTime - startTime;
               renderTimeSum += renderTime;
               renderCount++;
               if (DEBUG && renderCount % 60 == 0) {
                  renderTimeSum = 0L;
                  renderCount = 0;
               }
            }
         }
      }
   }

   public static void triggerKillIcon(String weaponName, String targetName, boolean isCritical, boolean playUltimateSound, int killScore, boolean isAssist) {
      if (Config.iconMode != Config.IconMode.CARD) {
         subtitleBrightness = 4.0F;
         subtitleScale = 1.5F;
         subtitleAnimationStartTime = System.currentTimeMillis();
      }

      long currentTime = System.currentTimeMillis();
      addHistoryRecord(weaponName, targetName, isCritical, isAssist, killScore);
      if (currentTime - lastComboTime > Config.comboTimeoutMs) {
         comboCount = 0;
         lastComboCount = 0;
         resetComboIfNeeded();
         if (comboKillItem != null) {
            activeScoreItems.remove(comboKillItem);
            comboKillItem = null;
         }
      }

      comboCount++;
      lastComboTime = currentTime;
      if (comboCount >= 2 && Config.iconMode != Config.IconMode.CARD) {
         int comboBonus = comboCount * 8;
         if (comboKillItem == null) {
            comboKillItem = new ScoreItemRenderer.ScoreItem("连杀 ! +", comboBonus, currentTime, false);
            comboKillItem.isComboKill = true;
            comboKillItem.comboNumber = comboCount;
            comboKillItem.displayDuration = 3000L;
            activeScoreItems.add(0, comboKillItem);
         } else {
            comboKillItem.targetPoints = comboBonus;
            comboKillItem.animationStartTime = currentTime;
            comboKillItem.initialPoints = comboKillItem.currentPoints;
            comboKillItem.comboNumber = comboCount;
            comboKillItem.startTime = currentTime;
         }
      }

      latestIsAssist = isAssist;
      latestIsCritical = isCritical;
      String reason = isAssist ? "助攻击败" : "击败生物";
      if (Config.iconMode != Config.IconMode.CARD) {
         addScore(killScore, reason, isCritical);
      }

      resetGroupDisplayTimer();
      switch (Config.iconMode) {
         case SCROLLING:
            scrollingRenderer.handleScrollingMode(weaponName, targetName, isCritical, playUltimateSound, isAssist);
            break;
         case COMBO:
            ComboIconRenderer.handleComboMode(weaponName, targetName, isCritical, isAssist);
            break;
         case CARD:
            cardRenderer.handleCardMode();
      }

      if (Config.iconMode == Config.IconMode.SCROLLING) {
         scrollingRenderer.updateAllIconTargetPositions();
      }

      lastKillTime = System.currentTimeMillis();
      isFadingOutAll = false;
      if (Config.iconMode != Config.IconMode.CARD) {
         latestWeaponName = weaponName;
         latestTargetName = targetName;
         textAnimationStartTime = currentTime;
      } else {
         latestWeaponName = "";
         latestTargetName = "";
         textAnimationStartTime = 0L;
      }

      textHideTime = 0L;
   }

   public static void addScore(float points, String reason, boolean isCritical) {
      if (!(points <= 0.0F)) {
         if (Config.showComboScore) {
            long currentTime = System.currentTimeMillis();
            lastDamageTime = currentTime;
            if (displayedComboScore == targetComboScore) {
               scoreAnimationStart = displayedComboScore;
            } else {
               scoreAnimationStart = targetComboScore;
            }

            int roundedPoints = Math.round(points);
            comboScore += roundedPoints;
            targetComboScore = comboScore;
            lastScoreUpdateTime = currentTime;
            boolean isLongRange = reason.startsWith("远距离击败");
            String expectedBaseText = RenderHelper.getString(reason);
            boolean found = false;

            for (ScoreItemRenderer.ScoreItem item : activeScoreItems) {
               if (item.baseText.equals(expectedBaseText) && (item.isLongRange && isLongRange || !item.isLongRange && !isLongRange)) {
                  item.initialPoints = item.currentPoints;
                  item.targetPoints += points;
                  item.animationStartTime = currentTime;
                  item.lastUpdateTime = currentTime;
                  item.startTime = currentTime;
                  if (isLongRange) {
                     String[] parts = reason.split(" ");
                     if (parts.length >= 2) {
                        item.distance = parts[1].replace("米", "m");
                     }
                  }

                  found = true;
                  break;
               }
            }

            if (!found) {
               ScoreItemRenderer.ScoreItem newItem = new ScoreItemRenderer.ScoreItem(expectedBaseText, points, currentTime, isLongRange);
               newItem.initialPoints = 0.0F;
               newItem.animationStartTime = currentTime;
               if (isLongRange) {
                  String[] parts = reason.split(" ");
                  if (parts.length >= 2) {
                     newItem.distance = parts[1].replace("米", "m");
                  }
               }

               activeScoreItems.add(newItem);
            }

            if (isCritical && (reason.equals("击败生物") || reason.equals("助攻击败"))) {
               float critBonus = points * 0.25F;
               int roundedCritBonus = Math.round(critBonus);
               comboScore += roundedCritBonus;
               targetComboScore = comboScore;
               boolean critFound = false;

               for (ScoreItemRenderer.ScoreItem itemx : activeScoreItems) {
                  if (itemx.baseText.equals("暴击加成 +")) {
                     itemx.initialPoints = itemx.currentPoints;
                     itemx.targetPoints += critBonus;
                     itemx.animationStartTime = currentTime;
                     itemx.lastUpdateTime = currentTime;
                     itemx.startTime = currentTime;
                     critFound = true;
                     break;
                  }
               }

               if (!critFound) {
                  ScoreItemRenderer.ScoreItem critItem = new ScoreItemRenderer.ScoreItem("暴击加成 +", critBonus, currentTime, false);
                  critItem.initialPoints = 0.0F;
                  critItem.animationStartTime = currentTime;
                  activeScoreItems.add(critItem);
               }
            }

            if (!isComboScoreVisible) {
               isComboScoreVisible = true;
               comboScoreShowTime = currentTime;
            }
         }
      }
   }

   public static void showLongRangeBonus(int distance, int bonusPoints) {
      longRangeDistance = distance;
      longRangeBonus = bonusPoints;
      longRangeShowTime = System.currentTimeMillis();
   }

   public static void clearCardCombo() {
      activeCards.clear();
      cardComboCount = 0;
      lastKillTime = 0L;
   }

   public static void resetComboIfNeeded() {
      if (shouldResetCombo()) {
         cardComboCount = 0;
         lastKillTime = 0L;
      }
   }

   public static boolean shouldResetCombo() {
      if (Config.iconMode == Config.IconMode.CARD) {
         if (CardModeRenderer.getKillChainTimeout() == 0) {
            return false;
         } else {
            long currentTime = System.currentTimeMillis();
            return currentTime - lastKillTime > CardModeRenderer.getKillChainTimeout();
         }
      } else {
         long currentTime = System.currentTimeMillis();
         return currentTime - lastKillTime > 3000L;
      }
   }

   private static void addDebugInfo(DrawContext guiGraphics) {
      if (Config.debugShowInfo) {
         MinecraftClient mc = MinecraftClient.getInstance();
         if (mc.player != null && mc.world != null) {
            List<String> debugInfo = Arrays.asList(
               "GD656Killicon 调试信息",
               "活动图标: " + activeIcons.size(),
               "连击分数: " + displayedComboScore + "/" + targetComboScore,
               "连击计数: " + comboCount,
               "最后伤害: " + (System.currentTimeMillis() - lastDamageTime) + "ms前",
               "活动分数项: " + activeScoreItems.size()
            );
            int y = 10;

            for (String info : debugInfo) {
               guiGraphics.drawText(mc.textRenderer, info, 10, y, 16777215, false);
               y += 10;
            }

            for (int i = 0; i < activeScoreItems.size(); i++) {
               ScoreItemRenderer.ScoreItem item = activeScoreItems.get(i);
               String itemInfo = String.format("项 %d: %.1f/%.1f (%s)", i, item.currentPoints, item.targetPoints, item.baseText);
               guiGraphics.drawText(mc.textRenderer, itemInfo, 10, y, 16777215, false);
               y += 10;
            }
         }
      }
   }

   private static void addHistoryRecord(String weaponName, String targetName, boolean isCritical, boolean isAssist, int killScore) {
      try {
         String damageInfo;
         if (isAssist) {
            damageInfo = "助攻";
         } else if (isCritical) {
            damageInfo = "暴击击杀";
         } else {
            damageInfo = "击杀";
         }

         if (killScore > 0) {
            damageInfo = damageInfo + " +" + killScore;
         }

         boolean isPlayer = isPlayerTarget(targetName);
         HistoryRecordScreen.addHistoryRecord(targetName, damageInfo, weaponName, isPlayer, !isAssist);
      } catch (Exception var7) {
         LOGGER.warn("[六五六] 添加历史记录失败: {}", var7.getMessage());
      }
   }

   private static boolean isPlayerTarget(String targetName) {
      try {
         MinecraftClient mc = MinecraftClient.getInstance();
         if (mc.world != null && mc.player != null) {
            if (targetName.equals(mc.player.getGameProfile().name())) {
               return true;
            }

            for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
               if (targetName.equals(player.getGameProfile().name())) {
                  return true;
               }
            }
         }

         return targetName.length() <= 16
            && !targetName.contains(" ")
            && !targetName.contains(".")
            && !targetName.contains("_" + UUID.randomUUID().toString().substring(0, 8));
      } catch (Exception var5) {
         return false;
      }
   }

   private static void renderLongRangeBonus(DrawContext guiGraphics, long currentTime) {
      if (currentTime - longRangeShowTime <= 3000L) {
         MinecraftClient mc = MinecraftClient.getInstance();
         TextRenderer font = mc.textRenderer;
         int screenWidth = mc.getWindow().getScaledWidth();
         int screenHeight = mc.getWindow().getScaledHeight();
         float progress = (float)(currentTime - longRangeShowTime) / 3000.0F;
         float alpha = 1.0F;
         if (progress > 0.8F) {
            alpha = 1.0F - (progress - 0.8F) / 0.2F;
         }

         int textY = screenHeight - 100;
         String text = "远距离击败";
         String distanceText = longRangeDistance + "m";
         String bonusText = " +" + longRangeBonus;
         int textWidth = font.getWidth(text);
         int distanceWidth = font.getWidth(distanceText);
         int bonusWidth = font.getWidth(bonusText);
         int totalWidth = textWidth + distanceWidth + bonusWidth;
         int textX = screenWidth / 2 - totalWidth / 2;
         int whiteColor = 16777215 | (int)(alpha * 255.0F) << 24;
         guiGraphics.drawText(font, text, textX, textY, whiteColor, true);
         textX += textWidth;
         int goldColor = -10496;
         goldColor = goldColor & 16777215 | (int)(alpha * 255.0F) << 24;
         guiGraphics.drawText(font, distanceText, textX, textY, goldColor, true);
         textX += distanceWidth;
         guiGraphics.drawText(font, bonusText, textX, textY, whiteColor, true);
      }
   }

   private static void resetGroupDisplayTimer() {
      groupDisplayStartTime = System.currentTimeMillis();
      isGroupDisplaying = true;
      shouldStartFadeOut = false;
      isFadingOutAll = false;

      for (ScrollingIconRenderer.KillIconInstance icon : activeIcons) {
         icon.fadeStartTime = -1L;
         icon.alpha = 1.0F;
      }

      if (Config.debugShowInfo) {
         LOGGER.info("[六五六] 重置组显示计时器，图标数量: {}", activeIcons.size());
      }
   }

   private static void updateGroupDisplayTimer(long currentTime) {
      if (isGroupDisplaying && !activeIcons.isEmpty()) {
         long displayDuration = Config.killIconDuration * 50L;
         if (currentTime - groupDisplayStartTime > displayDuration && !shouldStartFadeOut) {
            shouldStartFadeOut = true;
            scrollingRenderer.startFadeOutSequence(currentTime);
            if (Config.debugShowInfo) {
               LOGGER.info("[六五六] 触发淡出序列，显示时间: {}ms", displayDuration);
            }
         }
      }
   }
}
