package org.mods.gd656killicon.client.configmenu;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;

public class FeedbackScreen extends Screen {
   private final Screen parent;
   private static final int BUTTON_WIDTH = 100;
   private static final int BUTTON_HEIGHT = 20;

   public FeedbackScreen(Screen parent) {
      super(Text.literal("反馈"));
      this.parent = parent;
   }

   protected void init() {
      super.init();
      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, button -> {
         if (this.client != null) {
            this.client.setScreen(this.parent);
         }
      }).dimensions(10, 10, 60, 20).build());
      this.addDrawableChild(ButtonWidget.builder(Text.literal("我要反馈"), button -> {
         if (this.client != null) {
            Util.getOperatingSystem().open("https://bug.flna.top/");
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
      guiGraphics.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§c如有问题或建议，请通过以下方式联系我们:"), centerX, centerY - 20, 16777215);
      guiGraphics.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§a加入我们的官方QQ群: 1033096961"), centerX, centerY + 20, 16777215);
      guiGraphics.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§c感谢您的支持！"), centerX, centerY + 40, 16777215);
   }
}
