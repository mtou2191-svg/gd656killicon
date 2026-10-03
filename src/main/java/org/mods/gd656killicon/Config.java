package org.mods.gd656killicon;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

public class Config {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("gd656killicon.json");
   private static Config INSTANCE;
   public int SCROLL_ICON_X;
   public int SCROLL_ICON_Y;
   public int SCROLL_SUBTITLE_X;
   public int SCROLL_SUBTITLE_Y;
   public int SCROLL_SCORE_X;
   public int SCROLL_SCORE_Y;
   public int SCROLL_BONUS_X;
   public int SCROLL_BONUS_Y;
   public int COMBO_ICON_X;
   public int COMBO_ICON_Y;
   public int COMBO_SUBTITLE_X;
   public int COMBO_SUBTITLE_Y;
   public int COMBO_SCORE_X;
   public int COMBO_SCORE_Y;
   public int COMBO_BONUS_X;
   public int COMBO_BONUS_Y;
   public int CARD_ICON_X;
   public int CARD_ICON_Y;
   public int CARD_SUBTITLE_X;
   public int CARD_SUBTITLE_Y;
   public int CARD_SCORE_X;
   public int CARD_SCORE_Y;
   public int CARD_BONUS_X;
   public int CARD_BONUS_Y;
   public int SCROLL_ICON_SIZE;
   public int SCROLL_SUBTITLE_SIZE;
   public int SCROLL_SCORE_SIZE;
   public int SCROLL_BONUS_SIZE;
   public boolean SCROLL_ICON_VISIBLE;
   public boolean SCROLL_SUBTITLE_VISIBLE;
   public boolean SCROLL_SCORE_VISIBLE;
   public boolean SCROLL_BONUS_VISIBLE;
   public int COMBO_ICON_SIZE;
   public int COMBO_SUBTITLE_SIZE;
   public int COMBO_SCORE_SIZE;
   public int COMBO_BONUS_SIZE;
   public boolean COMBO_ICON_VISIBLE;
   public boolean COMBO_SUBTITLE_VISIBLE;
   public boolean COMBO_SCORE_VISIBLE;
   public boolean COMBO_BONUS_VISIBLE;
   public int CARD_ICON_SIZE;
   public int CARD_SUBTITLE_SIZE;
   public int CARD_SCORE_SIZE;
   public int CARD_BONUS_SIZE;
   public boolean CARD_ICON_VISIBLE;
   public boolean CARD_SUBTITLE_VISIBLE;
   public boolean CARD_SCORE_VISIBLE;
   public boolean CARD_BONUS_VISIBLE;
   public String KILL_BONUS_DISPLAY;
   public String ASSIST_BONUS_DISPLAY;
   public String CRITICAL_BONUS_DISPLAY;
   public String LONGRANGE_BONUS_DISPLAY;
   public String MAGIC_BONUS_DISPLAY;
   public String HAND_BONUS_DISPLAY;
   public String COMBO_BONUS_DISPLAY;
   public String DAMAGE_BONUS_DISPLAY;
   public Config.IconStyle CARD_ICON_STYLE;
   public int KILL_ICON_DURATION;
   public int KILL_ICON_SIZE;
   public static String WEAPON_COLOR;
   public static String TARGET_COLOR;
   public String CUSTOM_SUBTITLE_FORMAT;
   public int SOUND_VOLUME;
   public static String CRITICAL_COLOR;
   public Config.IconMode ICON_MODE;
   public int COMBO_SCORE_DURATION;
   public int COMBO_SCORE_FONT_SCALE;
   public static String FLASHING_COLOR;
   public static String HIGH_SCORE_COLOR;
   public int COMBO_TIMEOUT;
   public double SCROLL_ANIMATION_SPEED;
   public String ASSIST_SUBTITLE_FORMAT;
   public boolean DEBUG_SHOW_INFO;
   public static double SCROLL_SENSITIVITY;
   public Config.IconStyle ICON_STYLE;
   public int KILL_ICON_ANIMATION_DURATION;
   public int KILL_ICON_SPACING;
   public int FORCE_HIDE_COUNT;
   public int MAX_DISPLAY_COUNT;
   public int BONUS_LINE_SPACING;
   public boolean ENABLE_SOUND_EFFECTS;
   public int CARD_BOTTOMBAR_SIZE;
   public int CARD_BOTTOMBAR_OFFSET_Y;
   public boolean CARD_BOTTOMBAR_VISIBLE;
   public boolean COMBO_ICON_ANIMATION_ENABLED;
   public int COMBO_ICON_ANIMATION_DURATION;
   public int COMBO_ICON_ANIMATION_TOTAL_FRAMES;
   public static int scrollIconX;
   public static int scrollIconY;
   public static int scrollSubtitleX;
   public static int scrollSubtitleY;
   public static int scrollScoreX;
   public static int scrollScoreY;
   public static int scrollBonusX;
   public static int scrollBonusY;
   public static int comboIconX;
   public static int comboIconY;
   public static int comboSubtitleX;
   public static int comboSubtitleY;
   public static int comboScoreX;
   public static int comboScoreY;
   public static int comboBonusX;
   public static int comboBonusY;
   public static int cardIconX;
   public static int cardIconY;
   public static int cardSubtitleX;
   public static int cardSubtitleY;
   public static int cardScoreX;
   public static int cardScoreY;
   public static int cardBonusX;
   public static int cardBonusY;
   public static int scrollIconSize;
   public static int scrollSubtitleSize;
   public static int scrollScoreSize;
   public static int scrollBonusSize;
   public static boolean scrollIconVisible;
   public static boolean scrollSubtitleVisible;
   public static boolean scrollScoreVisible;
   public static boolean scrollBonusVisible;
   public static int comboIconSize;
   public static int comboSubtitleSize;
   public static int comboScoreSize;
   public static int comboBonusSize;
   public static boolean comboIconVisible;
   public static boolean comboSubtitleVisible;
   public static boolean comboScoreVisible;
   public static boolean comboBonusVisible;
   public static int cardIconSize;
   public static int cardSubtitleSize;
   public static int cardScoreSize;
   public static int cardBonusSize;
   public static boolean cardIconVisible;
   public static boolean cardSubtitleVisible;
   public static boolean cardScoreVisible;
   public static boolean cardBonusVisible;
   public static int cardBottombarSize;
   public static int cardBottombarOffsetY;
   public static boolean cardBottombarVisible;
   public static int killIconDuration;
   public static int killIconSize;
   public static int weaponColor = -16744320;
   public static int targetColor = -16744320;
   public static boolean showKillIcons = true;
   public static boolean showKillSubtitles = true;
   public static String customSubtitleFormat = "你 使用 {weapon} 击败了 {target}";
   public static int soundVolume = 100;
   public static Config.IconMode iconMode = Config.IconMode.SCROLLING;
   public static Config.IconStyle iconStyle = Config.IconStyle.MODERN;
   public static Config.IconStyle cardIconStyle = Config.IconStyle.MODERN;
   public static boolean showComboScore = true;
   public static int comboScoreDuration = 2000;
   public static int comboScoreFontScale = 250;
   public static int flashingColor = -2302756;
   public static int highScoreColor = -10496;
   public static int comboTimeout = 3;
   public static double scrollAnimationSpeed = 0.2;
   public static int comboTimeoutMs;
   public static int criticalColor;
   public static String assistSubtitleFormat = "你 助攻击败了 {target}";
   public static boolean debugShowInfo = false;
   public static int weaponColorHex = -16744320;
   public static int targetColorHex = -16744320;
   public static int criticalColorHex = -10496;
   public static int flashingColorHex = -2302756;
   public static int highScoreColorHex = -10496;
   public static String killBonusDisplay;
   public static String assistBonusDisplay;
   public static String criticalBonusDisplay;
   public static String longrangeBonusDisplay;
   public static String magicBonusDisplay;
   public static String handBonusDisplay;
   public static String comboBonusDisplay;
   public static String damageBonusDisplay;
   public static int killIconAnimationDuration;
   public static int killIconSpacing;
   public static int forceHideCount;
   public static int maxDisplayCount;
   public static int bonusLineSpacing;
   public static boolean enableSoundEffects = true;
   public static boolean comboIconAnimationEnabled;
   public static int comboIconAnimationDuration;
   public static int comboIconAnimationTotalFrames;

   public Config() {
      this.setupDefaultValues();
   }

   private void setupDefaultValues() {
      this.KILL_ICON_DURATION = 60;
      this.KILL_ICON_SIZE = 32;
      this.CUSTOM_SUBTITLE_FORMAT = "你 使用 {weapon} 击败了 {target}";
      this.ASSIST_SUBTITLE_FORMAT = "你 助攻击败了 {target}";
      this.ICON_MODE = Config.IconMode.SCROLLING;
      this.ICON_STYLE = Config.IconStyle.MODERN;
      this.CARD_ICON_STYLE = Config.IconStyle.MODERN;
      this.KILL_ICON_ANIMATION_DURATION = 300;
      this.KILL_ICON_SPACING = 10;
      this.FORCE_HIDE_COUNT = 9;
      this.MAX_DISPLAY_COUNT = 30;
      this.setupDefaultPositionValues();
      this.setupDefaultElementValues();
      this.setupDefaultBonusValues();
      this.setupDefaultColorValues();
      this.setupDefaultSoundValues();
      this.setupDefaultComboValues();
      this.setupDefaultDebugValues();
   }

   private void setupDefaultPositionValues() {
      this.SCROLL_ICON_X = 0;
      this.SCROLL_ICON_Y = -140;
      this.SCROLL_SUBTITLE_X = 0;
      this.SCROLL_SUBTITLE_Y = -100;
      this.SCROLL_SCORE_X = 0;
      this.SCROLL_SCORE_Y = -80;
      this.SCROLL_BONUS_X = 0;
      this.SCROLL_BONUS_Y = -60;
      this.COMBO_ICON_X = 0;
      this.COMBO_ICON_Y = -140;
      this.COMBO_SUBTITLE_X = 0;
      this.COMBO_SUBTITLE_Y = -100;
      this.COMBO_SCORE_X = 0;
      this.COMBO_SCORE_Y = -80;
      this.COMBO_BONUS_X = 0;
      this.COMBO_BONUS_Y = -60;
      this.CARD_ICON_X = 0;
      this.CARD_ICON_Y = -70;
      this.CARD_SUBTITLE_X = 0;
      this.CARD_SUBTITLE_Y = -100;
      this.CARD_SCORE_X = 0;
      this.CARD_SCORE_Y = -80;
      this.CARD_BONUS_X = 0;
      this.CARD_BONUS_Y = -60;
   }

   private void setupDefaultElementValues() {
      this.SCROLL_ICON_SIZE = 32;
      this.SCROLL_ICON_VISIBLE = true;
      this.SCROLL_SUBTITLE_SIZE = 100;
      this.SCROLL_SUBTITLE_VISIBLE = true;
      this.SCROLL_SCORE_SIZE = 250;
      this.SCROLL_SCORE_VISIBLE = true;
      this.SCROLL_BONUS_SIZE = 100;
      this.SCROLL_BONUS_VISIBLE = true;
      this.COMBO_ICON_SIZE = 32;
      this.COMBO_ICON_VISIBLE = true;
      this.COMBO_SUBTITLE_SIZE = 100;
      this.COMBO_SUBTITLE_VISIBLE = true;
      this.COMBO_SCORE_SIZE = 250;
      this.COMBO_SCORE_VISIBLE = true;
      this.COMBO_BONUS_SIZE = 100;
      this.COMBO_BONUS_VISIBLE = true;
      this.COMBO_ICON_ANIMATION_ENABLED = false;
      this.COMBO_ICON_ANIMATION_DURATION = 2000;
      this.COMBO_ICON_ANIMATION_TOTAL_FRAMES = 11;
      this.CARD_ICON_SIZE = 48;
      this.CARD_ICON_VISIBLE = true;
      this.CARD_SUBTITLE_SIZE = 100;
      this.CARD_SUBTITLE_VISIBLE = false;
      this.CARD_SCORE_SIZE = 250;
      this.CARD_SCORE_VISIBLE = false;
      this.CARD_BONUS_SIZE = 100;
      this.CARD_BONUS_VISIBLE = false;
      this.CARD_BOTTOMBAR_SIZE = 80;
      this.CARD_BOTTOMBAR_VISIBLE = true;
      this.CARD_BOTTOMBAR_OFFSET_Y = 30;
   }

   private void setupDefaultBonusValues() {
      this.KILL_BONUS_DISPLAY = "击败生物 +{score}";
      this.ASSIST_BONUS_DISPLAY = "助攻击败 +{score}";
      this.CRITICAL_BONUS_DISPLAY = "暴击加成 +{score}";
      this.MAGIC_BONUS_DISPLAY = "魔法伤害 +{score}";
      this.HAND_BONUS_DISPLAY = "空手攻击 +{score}";
      this.DAMAGE_BONUS_DISPLAY = "造成伤害 +{score}";
      this.LONGRANGE_BONUS_DISPLAY = "远距离击败 [6]{distance}[f] +{score}";
      this.COMBO_BONUS_DISPLAY = "[6]{combo}[f] 连杀 ! +{score}";
      this.BONUS_LINE_SPACING = 12;
   }

   private void setupDefaultColorValues() {
      WEAPON_COLOR = "008080";
      TARGET_COLOR = "008080";
      CRITICAL_COLOR = "FFD700";
      FLASHING_COLOR = "DCDCDC";
      HIGH_SCORE_COLOR = "FFD700";
   }

   private void setupDefaultSoundValues() {
      this.ENABLE_SOUND_EFFECTS = true;
      this.SOUND_VOLUME = 100;
   }

   private void setupDefaultComboValues() {
      this.COMBO_SCORE_DURATION = 2000;
      this.COMBO_SCORE_FONT_SCALE = 250;
      this.COMBO_TIMEOUT = 3;
      this.SCROLL_ANIMATION_SPEED = 0.2;
   }

   private void setupDefaultDebugValues() {
      this.DEBUG_SHOW_INFO = false;
      SCROLL_SENSITIVITY = 1.0;
   }

   public static void loadConfig() {
      try {
         if (Files.exists(CONFIG_PATH)) {
            String content = Files.readString(CONFIG_PATH);
            INSTANCE = (Config)GSON.fromJson(content, Config.class);
         } else {
            INSTANCE = new Config();
            saveConfig();
         }

         loadRuntimeVariables();
      } catch (Exception var1) {
         LOGGER.error("[六五六] 加载配置时发生错误: {}", var1.getMessage());
         INSTANCE = new Config();
         loadRuntimeVariables();
      }
   }

   public static void resetAllToDefaults() {
      try {
         INSTANCE = new Config();
         loadRuntimeVariables();
         safeSaveConfig();
      } catch (Exception var1) {
         LOGGER.error("[六五六] 重置配置时发生错误: {}", var1.getMessage());
         loadConfig();
      }
   }

   private static int parseColor(String hex) {
      if (hex != null && !hex.isEmpty()) {
         try {
            String cleanHex = hex.replace("#", "");
            if (cleanHex.length() == 3) {
               cleanHex = "" + cleanHex.charAt(0) + cleanHex.charAt(0) + cleanHex.charAt(1) + cleanHex.charAt(1) + cleanHex.charAt(2) + cleanHex.charAt(2);
            }

            return (int)Long.parseLong("FF" + cleanHex, 16);
         } catch (Exception var2) {
            LOGGER.error("Invalid color format: {}", hex);
            return -16744320;
         }
      } else {
         return -16744320;
      }
   }

   public static void reloadCriticalColor() {
      criticalColor = parseColor(CRITICAL_COLOR);
   }

   private static void safeSaveConfig() {
      try {
         if (INSTANCE == null) {
            INSTANCE = new Config();
         }

         String json = GSON.toJson(INSTANCE);
         Files.createDirectories(CONFIG_PATH.getParent());
         Files.writeString(CONFIG_PATH, json);
      } catch (IOException var1) {
         LOGGER.error("[六五六] 保存配置时发生错误: {}", var1.getMessage());
      }
   }

   public static void saveConfig() {
      safeSaveConfig();
   }

   public static void loadRuntimeVariables() {
      if (INSTANCE != null) {
         loadGeneralConfigs();
         loadPositionConfigs();
         loadElementConfigs();
         loadColorConfigs();
         loadSoundConfigs();
         reloadCriticalColor();
      }
   }

   private static void loadGeneralConfigs() {
      killIconDuration = INSTANCE.KILL_ICON_DURATION;
      killIconSize = INSTANCE.KILL_ICON_SIZE;
      customSubtitleFormat = INSTANCE.CUSTOM_SUBTITLE_FORMAT;
      soundVolume = INSTANCE.SOUND_VOLUME;
      iconMode = INSTANCE.ICON_MODE;
      iconStyle = INSTANCE.ICON_STYLE;
      comboScoreDuration = INSTANCE.COMBO_SCORE_DURATION;
      comboScoreFontScale = INSTANCE.COMBO_SCORE_FONT_SCALE;
      comboTimeout = INSTANCE.COMBO_TIMEOUT * 1000;
      scrollAnimationSpeed = INSTANCE.SCROLL_ANIMATION_SPEED;
      comboTimeoutMs = INSTANCE.COMBO_TIMEOUT * 1000;
      assistSubtitleFormat = INSTANCE.ASSIST_SUBTITLE_FORMAT;
      killBonusDisplay = INSTANCE.KILL_BONUS_DISPLAY;
      assistBonusDisplay = INSTANCE.ASSIST_BONUS_DISPLAY;
      criticalBonusDisplay = INSTANCE.CRITICAL_BONUS_DISPLAY;
      magicBonusDisplay = INSTANCE.MAGIC_BONUS_DISPLAY;
      handBonusDisplay = INSTANCE.HAND_BONUS_DISPLAY;
      damageBonusDisplay = INSTANCE.DAMAGE_BONUS_DISPLAY;
      longrangeBonusDisplay = INSTANCE.LONGRANGE_BONUS_DISPLAY;
      comboBonusDisplay = INSTANCE.COMBO_BONUS_DISPLAY;
      killIconAnimationDuration = INSTANCE.KILL_ICON_ANIMATION_DURATION;
      killIconSpacing = INSTANCE.KILL_ICON_SPACING;
      forceHideCount = INSTANCE.FORCE_HIDE_COUNT;
      maxDisplayCount = INSTANCE.MAX_DISPLAY_COUNT;
      bonusLineSpacing = INSTANCE.BONUS_LINE_SPACING;
      cardIconStyle = INSTANCE.CARD_ICON_STYLE;
      enableSoundEffects = INSTANCE.ENABLE_SOUND_EFFECTS;
      cardBottombarSize = INSTANCE.CARD_BOTTOMBAR_SIZE;
      cardBottombarVisible = INSTANCE.CARD_BOTTOMBAR_VISIBLE;
      cardBottombarOffsetY = INSTANCE.CARD_BOTTOMBAR_OFFSET_Y;
      comboIconAnimationEnabled = INSTANCE.COMBO_ICON_ANIMATION_ENABLED;
      comboIconAnimationDuration = INSTANCE.COMBO_ICON_ANIMATION_DURATION;
      comboIconAnimationTotalFrames = INSTANCE.COMBO_ICON_ANIMATION_TOTAL_FRAMES;
   }

   private static void loadPositionConfigs() {
      scrollIconX = INSTANCE.SCROLL_ICON_X;
      scrollIconY = INSTANCE.SCROLL_ICON_Y;
      scrollSubtitleX = INSTANCE.SCROLL_SUBTITLE_X;
      scrollSubtitleY = INSTANCE.SCROLL_SUBTITLE_Y;
      scrollScoreX = INSTANCE.SCROLL_SCORE_X;
      scrollScoreY = INSTANCE.SCROLL_SCORE_Y;
      scrollBonusX = INSTANCE.SCROLL_BONUS_X;
      scrollBonusY = INSTANCE.SCROLL_BONUS_Y;
      comboIconX = INSTANCE.COMBO_ICON_X;
      comboIconY = INSTANCE.COMBO_ICON_Y;
      comboSubtitleX = INSTANCE.COMBO_SUBTITLE_X;
      comboSubtitleY = INSTANCE.COMBO_SUBTITLE_Y;
      comboScoreX = INSTANCE.COMBO_SCORE_X;
      comboScoreY = INSTANCE.COMBO_SCORE_Y;
      comboBonusX = INSTANCE.COMBO_BONUS_X;
      comboBonusY = INSTANCE.COMBO_BONUS_Y;
      cardIconX = INSTANCE.CARD_ICON_X;
      cardIconY = INSTANCE.CARD_ICON_Y;
      cardSubtitleX = INSTANCE.CARD_SUBTITLE_X;
      cardSubtitleY = INSTANCE.CARD_SUBTITLE_Y;
      cardScoreX = INSTANCE.CARD_SCORE_X;
      cardScoreY = INSTANCE.CARD_SCORE_Y;
      cardBonusX = INSTANCE.CARD_BONUS_X;
      cardBonusY = INSTANCE.CARD_BONUS_Y;
   }

   private static void loadElementConfigs() {
      scrollIconSize = INSTANCE.SCROLL_ICON_SIZE;
      scrollIconVisible = INSTANCE.SCROLL_ICON_VISIBLE;
      scrollSubtitleSize = INSTANCE.SCROLL_SUBTITLE_SIZE;
      scrollSubtitleVisible = INSTANCE.SCROLL_SUBTITLE_VISIBLE;
      scrollScoreSize = INSTANCE.SCROLL_SCORE_SIZE;
      scrollScoreVisible = INSTANCE.SCROLL_SCORE_VISIBLE;
      scrollBonusSize = INSTANCE.SCROLL_BONUS_SIZE;
      scrollBonusVisible = INSTANCE.SCROLL_BONUS_VISIBLE;
      comboIconSize = INSTANCE.COMBO_ICON_SIZE;
      comboIconVisible = INSTANCE.COMBO_ICON_VISIBLE;
      comboSubtitleSize = INSTANCE.COMBO_SUBTITLE_SIZE;
      comboSubtitleVisible = INSTANCE.COMBO_SUBTITLE_VISIBLE;
      comboScoreSize = INSTANCE.COMBO_SCORE_SIZE;
      comboScoreVisible = INSTANCE.COMBO_SCORE_VISIBLE;
      comboBonusSize = INSTANCE.COMBO_BONUS_SIZE;
      comboBonusVisible = INSTANCE.COMBO_BONUS_VISIBLE;
      cardIconSize = INSTANCE.CARD_ICON_SIZE;
      cardIconVisible = INSTANCE.CARD_ICON_VISIBLE;
      cardSubtitleSize = INSTANCE.CARD_SUBTITLE_SIZE;
      cardSubtitleVisible = INSTANCE.CARD_SUBTITLE_VISIBLE;
      cardScoreSize = INSTANCE.CARD_SCORE_SIZE;
      cardScoreVisible = INSTANCE.CARD_SCORE_VISIBLE;
      cardBonusSize = INSTANCE.CARD_BONUS_SIZE;
      cardBonusVisible = INSTANCE.CARD_BONUS_VISIBLE;
   }

   private static void loadColorConfigs() {
      weaponColor = parseColor(WEAPON_COLOR);
      targetColor = parseColor(TARGET_COLOR);
      criticalColor = parseColor(CRITICAL_COLOR);
      flashingColor = parseColor(FLASHING_COLOR);
      highScoreColor = parseColor(HIGH_SCORE_COLOR);
      weaponColorHex = parseColor(WEAPON_COLOR);
      targetColorHex = parseColor(TARGET_COLOR);
      criticalColorHex = parseColor(CRITICAL_COLOR);
      flashingColorHex = parseColor(FLASHING_COLOR);
      highScoreColorHex = parseColor(HIGH_SCORE_COLOR);
   }

   private static void loadSoundConfigs() {
      enableSoundEffects = INSTANCE.ENABLE_SOUND_EFFECTS;
      soundVolume = INSTANCE.SOUND_VOLUME;
   }

   public static Config getInstance() {
      if (INSTANCE == null) {
         loadConfig();
      }

      return INSTANCE;
   }

   public static void initialize() {
      loadConfig();
   }

   public static enum IconMode {
      SCROLLING("gd656killicon.config.mode.scrolling"),
      COMBO("gd656killicon.config.mode.combo"),
      CARD("gd656killicon.config.mode.card");

      private final String translationKey;

      private IconMode(String translationKey) {
         this.translationKey = translationKey;
      }

      public String getTranslationKey() {
         return this.translationKey;
      }
   }

   public static enum IconStyle {
      VANILLA("gd656killicon.config.style.vanilla"),
      MODERN("gd656killicon.config.style.modern");

      private final String translationKey;

      private IconStyle(String translationKey) {
         this.translationKey = translationKey;
      }

      public String getTranslationKey() {
         return this.translationKey;
      }
   }
}
