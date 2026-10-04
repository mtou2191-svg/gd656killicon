package org.mods.gd656killicon.client.configmenu;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;

public class AboutScreen extends Screen {
   private final Screen parent;
   private static final int BUTTON_WIDTH = 100;
   private static final int BUTTON_HEIGHT = 20;

   public AboutScreen(Screen parent) {
      super(Text.literal("关于"));
      this.parent = parent;
   }

   protected void init() {
      super.init();
      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, button -> {
         if (this.client != null) {
            this.client.setScreen(this.parent);
         }
      }).dimensions(10, 10, 60, 20).build());
      this.addDrawableChild(ButtonWidget.builder(Text.literal("前往赞助！"), button -> {
         if (this.client != null) {
            this.client.keyboard.setClipboard("https://space.bilibili.com/516946949");
            Util.getOperatingSystem().open("https://space.bilibili.com/516946949");
         }
      }).dimensions(this.width / 2 - 50, this.height - 30, 100, 20).build());
   }

   public void close() {
      if (this.client != null) {
         this.client.setScreen(this.parent);
      }
   }

   public void render(@NotNull DrawContext guiGraphics, int mouseX, int mouseY, float partialTick) {
      super.render(guiGraphics, mouseX, mouseY, partialTick);
      int centerX = this.width / 2;
      int centerY = this.height / 2;
      guiGraphics.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§bGD656Killicon §7- §e完全免费"), centerX, centerY - 30, -1);
      guiGraphics.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§a版本: 1.0.0 公测版 RC5 *Fabric* §7| §6作者: Minecraft_GD656"), centerX, centerY - 10, -1);
      guiGraphics.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§7支持Minecraft: 1.20.1"), centerX, centerY + 10, -1);
      guiGraphics.drawCenteredTextWithShadow(this.textRenderer, Text.literal("此模组完全免费 若发现倒卖盈利者请立即告知"), centerX, centerY + 60, -1);
   }
}
