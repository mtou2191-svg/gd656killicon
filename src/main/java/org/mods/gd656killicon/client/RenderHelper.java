package org.mods.gd656killicon.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.mods.gd656killicon.Config;
import org.mods.gd656killicon.client.iconrenderer.ComboIconRenderer;

@Environment(EnvType.CLIENT)
public class RenderHelper {
   public static void drawBorder(DrawContext ctx, int x, int y, int width, int height, int color) {
      ctx.fill(x, y, x + width, y + 1, color);
      ctx.fill(x, y + height - 1, x + width, y + height, color);
      ctx.fill(x, y, x + 1, y + height, color);
      ctx.fill(x + width - 1, y, x + width, y + height, color);
   }

   public void loadResources() {
      this.loadIconResources();
      this.loadCardResources();
      KillIconRenderer.currentIconStyle = Config.iconStyle;
      KillIconRenderer.currentCardIconStyle = Config.cardIconStyle;
   }

   @NotNull
   public static String getString(String reason) {
      return switch (reason) {
         case "造成伤害" -> "造成伤害 +";
         case "远距离击败" -> "远距离击败 +";
         case "助攻击败" -> "助攻击败 +";
         case "暴击加成" -> "暴击加成 +";
         case "魔法伤害" -> "魔法伤害 +";
         case "空手攻击" -> "空手攻击 +";
         case "击败生物" -> "击败生物 +";
         default -> "";
      };
   }

   public static boolean isElementVisible(ModConfigScreen.ElementType type) {
      if (type == ModConfigScreen.ElementType.ICON) {
         return switch (Config.iconMode) {
            case SCROLLING -> Config.scrollIconVisible;
            case COMBO -> Config.comboIconVisible;
            case CARD -> Config.cardIconVisible;
         };
      } else if (type == ModConfigScreen.ElementType.SUBTITLE) {
         return switch (Config.iconMode) {
            case SCROLLING -> Config.scrollSubtitleVisible;
            case COMBO -> Config.comboSubtitleVisible;
            case CARD -> Config.cardSubtitleVisible;
         };
      } else if (type == ModConfigScreen.ElementType.SCORE) {
         return switch (Config.iconMode) {
            case SCROLLING -> Config.scrollScoreVisible;
            case COMBO -> Config.comboScoreVisible;
            case CARD -> Config.cardScoreVisible;
         };
      } else if (type == ModConfigScreen.ElementType.BONUS) {
         return switch (Config.iconMode) {
            case SCROLLING -> Config.scrollBonusVisible;
            case COMBO -> Config.comboBonusVisible;
            case CARD -> Config.cardBonusVisible;
         };
      } else {
         return type != ModConfigScreen.ElementType.BOTTOMBAR ? true : Config.iconMode == Config.IconMode.CARD && Config.cardBottombarVisible;
      }
   }

   public static int getElementSize(ModConfigScreen.ElementType type) {
      if (type == ModConfigScreen.ElementType.ICON) {
         return switch (Config.iconMode) {
            case SCROLLING -> Config.scrollIconSize;
            case COMBO -> Config.comboIconSize;
            case CARD -> Config.cardIconSize;
         };
      } else if (type == ModConfigScreen.ElementType.SUBTITLE) {
         return switch (Config.iconMode) {
            case SCROLLING -> Config.scrollSubtitleSize;
            case COMBO -> Config.comboSubtitleSize;
            case CARD -> Config.cardSubtitleSize;
         };
      } else if (type == ModConfigScreen.ElementType.SCORE) {
         return switch (Config.iconMode) {
            case SCROLLING -> Config.scrollScoreSize;
            case COMBO -> Config.comboScoreSize;
            case CARD -> Config.cardScoreSize;
         };
      } else if (type == ModConfigScreen.ElementType.BONUS) {
         return switch (Config.iconMode) {
            case SCROLLING -> Config.scrollBonusSize;
            case COMBO -> Config.comboBonusSize;
            case CARD -> Config.cardBonusSize;
         };
      } else if (type == ModConfigScreen.ElementType.BOTTOMBAR) {
         return switch (Config.iconMode) {
            case SCROLLING, COMBO -> 100;
            case CARD -> Config.cardBottombarSize;
         };
      } else {
         return 100;
      }
   }

   private void loadIconResources() {
      if (KillIconRenderer.scrollingRenderer != null) {
         KillIconRenderer.scrollingRenderer.loadResources();
      }

      if (KillIconRenderer.comboRenderer != null) {
         ComboIconRenderer.loadResources();
      }
   }

   private void loadCardResources() {
      String cardStylePath = Config.cardIconStyle == Config.IconStyle.VANILLA ? "vanilla_gui" : "modern_gui";
      KillIconRenderer.currentCardIcons = new Identifier[5];

      for (int i = 0; i < 5; i++) {
         String cardFileName = i < 4 ? "killcard_" + (i + 1) + ".png" : "killcard_ace.png";
         KillIconRenderer.currentCardIcons[i] = Identifier.of("gd656killicon", "textures/gui/" + cardStylePath + "/cardmode/" + cardFileName);
      }
   }

   public static class PositionConfig {
      public int bonusX;
      public int bonusY;
      public int iconX;
      public int iconY;
      public int scoreX;
      public int scoreY;
      public int subtitleX;
      public int subtitleY;
   }
}
