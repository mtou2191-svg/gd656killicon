package org.mods.gd656killicon.client.configmenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.mods.gd656killicon.network.RankingDataRequestPacket;

public class RankingListScreen extends Screen {
   private final Screen parent;
   private static final int BUTTON_HEIGHT = 20;
   private static final int BUTTON_WIDTH = 60;
   private static final int PADDING = 10;
   private static final int ROWS_PER_PAGE = 32;
   private static final int TITLE_HEIGHT = 30;
   private static RankingListScreen instance;
   private ButtonWidget nextPageButton;
   private ButtonWidget prevPageButton;
   private int currentPage = 0;
   private boolean hasData = false;
   private boolean isLoading = true;
   private final boolean isInGame;
   private boolean rankingEnabled = true;
   private int totalPages = 0;
   private int currentMouseX;
   private int currentMouseY;
   private int rowHeight;
   private int tableHeight;
   private int tableWidth;
   private int tableX;
   private int tableY;
   private final List<RankingListScreen.RankingEntry> rankingEntries = new ArrayList<>();

   public RankingListScreen(Screen parent) {
      super(Text.literal("击杀图标排行榜"));
      this.parent = parent;
      instance = this;
      this.isInGame = MinecraftClient.getInstance().player != null;
   }

   protected void init() {
      super.init();
      this.tableWidth = this.width - 20;
      this.tableHeight = this.height - 30 - 20 - 30;
      this.tableX = 10;
      this.tableY = 40;
      this.rowHeight = 18;
      this.initButtons();
      this.loadRankingData();
   }

   public void close() {
      instance = null;
      MinecraftClient.getInstance().setScreen(this.parent);
   }

   public void render(@NotNull DrawContext guiGraphics, int mouseX, int mouseY, float partialTick) {
      this.currentMouseX = mouseX;
      this.currentMouseY = mouseY;
      guiGraphics.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, -1);
      if (!this.isInGame) {
         guiGraphics.drawCenteredTextWithShadow(this.textRenderer, "请在游戏内查看排行榜", this.width / 2, this.height / 2, 16733525);
      } else if (this.isLoading) {
         guiGraphics.drawCenteredTextWithShadow(this.textRenderer, "正在加载排行榜数据...", this.width / 2, this.height / 2, -1);
      } else if (!this.rankingEnabled) {
         guiGraphics.drawCenteredTextWithShadow(this.textRenderer, "榜单已被管理员关闭", this.width / 2, this.height / 2, 16733525);
      } else if (!this.hasData) {
         guiGraphics.drawCenteredTextWithShadow(this.textRenderer, "暂无数据", this.width / 2, this.height / 2, 8947848);
      } else {
         String pageInfo = "第 " + (this.currentPage + 1) + " 页 / 共 " + this.totalPages + " 页";
         guiGraphics.drawText(this.textRenderer, pageInfo, this.width / 2 - this.textRenderer.getWidth(pageInfo) / 2, 20, 13421772, true);
         this.renderRankingTable(guiGraphics);
      }

      super.render(guiGraphics, mouseX, mouseY, partialTick);
   }

   public static RankingListScreen getInstance() {
      return instance;
   }

   public void updateRankingData(Map<UUID, Integer> serverRankingData, boolean rankingEnabled) {
      this.rankingEntries.clear();
      this.rankingEnabled = rankingEnabled;
      if (!rankingEnabled) {
         this.hasData = false;
         this.isLoading = false;
         this.updateButtonStates();
      } else {
         if (serverRankingData != null && !serverRankingData.isEmpty()) {
            List<Entry<UUID, Integer>> sortedEntries = new ArrayList<>(serverRankingData.entrySet());
            sortedEntries.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
            int rank = 1;

            for (Entry<UUID, Integer> entry : sortedEntries) {
               this.rankingEntries.add(new RankingListScreen.RankingEntry(rank, this.getPlayerName(entry.getKey()), entry.getKey(), entry.getValue()));
               rank++;
            }

            this.hasData = true;
         } else {
            this.hasData = false;
         }

         this.totalPages = this.hasData ? (int)Math.ceil(this.rankingEntries.size() / 32.0) : 0;
         this.isLoading = false;
         this.updateButtonStates();
      }
   }

   private void initButtons() {
      int bottomY = this.height - 20 - 10;
      this.addDrawableChild(
         ButtonWidget.builder(Text.literal("返回"), button -> this.close())
            .dimensions(this.width / 2 - 30, bottomY, 60, 20)
            .build()
      );
      this.nextPageButton = ButtonWidget.builder(Text.literal("下一页"), button -> {
         if (this.currentPage < this.totalPages - 1) {
            this.currentPage++;
            this.updateButtonStates();
         }
      }).dimensions(this.width - 60 - 10, bottomY, 60, 20).build();
      this.addDrawableChild(this.nextPageButton);
      this.prevPageButton = ButtonWidget.builder(Text.literal("上一页"), button -> {
         if (this.currentPage > 0) {
            this.currentPage--;
            this.updateButtonStates();
         }
      }).dimensions(10, bottomY, 60, 20).build();
      this.addDrawableChild(this.prevPageButton);
      this.updateButtonStates();
   }

   private boolean isMouseOverRow(int mouseX, int mouseY, int rowY) {
      return mouseX >= this.tableX && mouseX <= this.tableX + this.tableWidth && mouseY >= rowY && mouseY <= rowY + this.rowHeight;
   }

   private void loadRankingData() {
      this.isLoading = true;
      this.hasData = false;
      this.rankingEnabled = true;
      if (this.isInGame) {
         ClientPlayNetworking.send(RankingDataRequestPacket.INSTANCE);
      } else {
         this.isLoading = false;
      }
   }

   private void renderRankingTable(DrawContext guiGraphics) {
      guiGraphics.fill(this.tableX, this.tableY, this.tableX + this.tableWidth, this.tableY + this.tableHeight, Integer.MIN_VALUE);
      this.renderTableHeader(guiGraphics);
      this.renderTableRows(guiGraphics);
   }

   private void renderRowData(DrawContext guiGraphics, RankingListScreen.RankingEntry entry, int rowY) {
      int textY = rowY + (this.rowHeight - 9) / 2;
      guiGraphics.drawText(this.textRenderer, String.valueOf(entry.rank), this.tableX + 10, textY, this.getRankColor(entry.rank), true);
      guiGraphics.drawText(this.textRenderer, entry.playerName, this.tableX + 80, textY, -1, true);
      guiGraphics.drawText(this.textRenderer, String.format("%,d", entry.score), this.tableX + 290, textY, -1, true);
   }

   private void renderTableHeader(DrawContext guiGraphics) {
      int headerY = this.tableY + 5;
      guiGraphics.fill(this.tableX, this.tableY, this.tableX + this.tableWidth, this.tableY + 25, -2143009724);
      guiGraphics.fill(this.tableX, this.tableY + 25, this.tableX + this.tableWidth, this.tableY + 26, -10066330);
      guiGraphics.drawText(this.textRenderer, "排名", this.tableX + 10, headerY, -1, true);
      guiGraphics.drawText(this.textRenderer, "玩家名称", this.tableX + 80, headerY, -1, true);
      guiGraphics.drawText(this.textRenderer, "分数", this.tableX + 290, headerY, -1, true);
      guiGraphics.fill(this.tableX + 70, this.tableY, this.tableX + 71, this.tableY + this.tableHeight, -10066330);
      guiGraphics.fill(this.tableX + 280, this.tableY, this.tableX + 281, this.tableY + this.tableHeight, -10066330);
   }

   private void renderTableRows(DrawContext guiGraphics) {
      int startIndex = this.currentPage * 32;
      int endIndex = Math.min(startIndex + 32, this.rankingEntries.size());

      for (int i = startIndex; i < endIndex; i++) {
         RankingListScreen.RankingEntry entry = this.rankingEntries.get(i);
         int rowY = this.tableY + 30 + (i - startIndex) * this.rowHeight;
         if ((i - startIndex) % 2 == 0) {
            guiGraphics.fill(this.tableX, rowY, this.tableX + this.tableWidth, rowY + this.rowHeight, 1075847200);
         }

         if (this.isMouseOverRow(this.currentMouseX, this.currentMouseY, rowY)) {
            guiGraphics.fill(this.tableX, rowY, this.tableX + this.tableWidth, rowY + this.rowHeight, 1080452710);
         }

         guiGraphics.fill(this.tableX, rowY + this.rowHeight, this.tableX + this.tableWidth, rowY + this.rowHeight + 1, 1078215748);
         this.renderRowData(guiGraphics, entry, rowY);
      }
   }

   private void updateButtonStates() {
      this.prevPageButton.active = this.currentPage > 0 && this.hasData && this.rankingEnabled;
      this.nextPageButton.active = this.currentPage < this.totalPages - 1 && this.hasData && this.rankingEnabled;
   }

   private String getPlayerName(UUID playerId) {
      try {
         if (this.client != null && this.client.world != null) {
            PlayerEntity player = this.client.world.getPlayerByUuid(playerId);
            if (player != null) {
               return player.getGameProfile().name();
            }
         }

         return "玩家_" + playerId.toString().substring(0, 8);
      } catch (Exception var3) {
         return "未知玩家";
      }
   }

   private int getRankColor(int rank) {
      return switch (rank) {
         case 1 -> -10496;
         case 2 -> -4144960;
         case 3 -> -3309774;
         default -> -1;
      };
   }

   private record RankingEntry(int rank, String playerName, UUID playerId, int score) {
   }
}
