package org.mods.gd656killicon.client.configmenu;

import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class HistoryRecordScreen extends Screen {
   private final Screen parent;
   private static final int BUTTON_HEIGHT = 20;
   private static final int BUTTON_WIDTH = 60;
   private static final int MAX_RECORDS = 32;
   private static final int PADDING = 10;
   private static final int TITLE_HEIGHT = 30;
   private static final CopyOnWriteArrayList<HistoryRecordScreen.HistoryRecord> historyRecords = new CopyOnWriteArrayList<>();
   private static HistoryRecordScreen instance;
   private ButtonWidget clearButton;
   private int currentMouseX;
   private int currentMouseY;
   private int rowHeight;
   private int tableHeight;
   private int tableWidth;
   private int tableX;
   private int tableY;
   private boolean hasRecords = false;

   public HistoryRecordScreen(Screen parent) {
      super(Text.literal("历史记录"));
      this.parent = parent;
      instance = this;
      this.updateRecordsState();
   }

   public static void addHistoryRecord(String entityName, String damageInfo, String weaponInfo, boolean isPlayer, boolean isKill) {
      historyRecords.add(0, new HistoryRecordScreen.HistoryRecord(entityName, damageInfo, weaponInfo, isPlayer, isKill));
      if (historyRecords.size() > 32) {
         historyRecords.remove(historyRecords.size() - 1);
      }

      if (instance != null) {
         instance.updateRecordsState();
         instance.updateButtonStates();
      }
   }

   public static HistoryRecordScreen getInstance() {
      return instance;
   }

   protected void init() {
      super.init();
      this.tableWidth = this.width - 20;
      this.tableHeight = this.height - 30 - 20 - 40;
      this.tableX = 10;
      this.tableY = 40;
      this.rowHeight = 18;
      this.initButtons();
   }

   public void close() {
      instance = null;
      MinecraftClient.getInstance().setScreen(this.parent);
   }

   public void render(@NotNull DrawContext guiGraphics, int mouseX, int mouseY, float partialTick) {
      this.currentMouseX = mouseX;
      this.currentMouseY = mouseY;
      guiGraphics.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, -1);
      if (!this.hasRecords) {
         guiGraphics.drawCenteredTextWithShadow(this.textRenderer, "暂无历史记录", this.width / 2, this.height / 2, 8947848);
      } else {
         String countInfo = "共 " + historyRecords.size() + " 条记录";
         guiGraphics.drawText(this.textRenderer, countInfo, this.width / 2 - this.textRenderer.getWidth(countInfo) / 2, 20, 13421772, true);
         this.renderHistoryTable(guiGraphics);
      }

      super.render(guiGraphics, mouseX, mouseY, partialTick);
   }

   private void clearHistoryRecords() {
      historyRecords.clear();
      this.updateRecordsState();
      this.updateButtonStates();
   }

   private int getMaxVisibleRows() {
      return (this.tableHeight - 30) / this.rowHeight;
   }

   private void initButtons() {
      int bottomY = this.height - 20 - 10;
      this.clearButton = ButtonWidget.builder(Text.literal("清空记录"), button -> this.clearHistoryRecords())
         .dimensions(10, bottomY, 60, 20)
         .build();
      this.addDrawableChild(this.clearButton);
      this.addDrawableChild(
         ButtonWidget.builder(Text.literal("返回"), button -> this.close())
            .dimensions(this.width - 60 - 10, bottomY, 60, 20)
            .build()
      );
      this.updateButtonStates();
   }

   private boolean isMouseOverRow(int mouseX, int mouseY, int rowY) {
      return mouseX >= this.tableX && mouseX <= this.tableX + this.tableWidth && mouseY >= rowY && mouseY <= rowY + this.rowHeight;
   }

   private void renderHistoryTable(DrawContext guiGraphics) {
      guiGraphics.fill(this.tableX, this.tableY, this.tableX + this.tableWidth, this.tableY + this.tableHeight, Integer.MIN_VALUE);
      this.renderTableHeader(guiGraphics);
      this.renderTableRows(guiGraphics);
   }

   private void renderRowData(DrawContext guiGraphics, HistoryRecordScreen.HistoryRecord record, int rowY) {
      int textY = rowY + (this.rowHeight - 9) / 2;
      guiGraphics.drawText(this.textRenderer, record.entityName, this.tableX + 10, textY, record.isPlayer ? -256 : -1, true);
      guiGraphics.drawText(this.textRenderer, record.damageInfo, this.tableX + 160, textY, record.isKill ? -43691 : -1, true);
      guiGraphics.drawText(this.textRenderer, record.weaponInfo, this.tableX + 260, textY, -1, true);
   }

   private void renderTableHeader(DrawContext guiGraphics) {
      int headerY = this.tableY + 5;
      guiGraphics.fill(this.tableX, this.tableY, this.tableX + this.tableWidth, this.tableY + 25, -2143009724);
      guiGraphics.fill(this.tableX, this.tableY + 25, this.tableX + this.tableWidth, this.tableY + 26, -10066330);
      guiGraphics.drawText(this.textRenderer, "实体名称", this.tableX + 10, headerY, -1, true);
      guiGraphics.drawText(this.textRenderer, "伤害信息", this.tableX + 160, headerY, -1, true);
      guiGraphics.drawText(this.textRenderer, "武器信息", this.tableX + 260, headerY, -1, true);
      guiGraphics.fill(this.tableX + 150, this.tableY, this.tableX + 151, this.tableY + this.tableHeight, -10066330);
      guiGraphics.fill(this.tableX + 250, this.tableY, this.tableX + 251, this.tableY + this.tableHeight, -10066330);
   }

   private void renderTableRows(DrawContext guiGraphics) {
      int visibleRows = Math.min(historyRecords.size(), this.getMaxVisibleRows());

      for (int i = 0; i < visibleRows; i++) {
         HistoryRecordScreen.HistoryRecord record = historyRecords.get(i);
         int rowY = this.tableY + 30 + i * this.rowHeight;
         if (i % 2 == 0) {
            guiGraphics.fill(this.tableX, rowY, this.tableX + this.tableWidth, rowY + this.rowHeight, 1075847200);
         }

         if (this.isMouseOverRow(this.currentMouseX, this.currentMouseY, rowY)) {
            guiGraphics.fill(this.tableX, rowY, this.tableX + this.tableWidth, rowY + this.rowHeight, 1080452710);
         }

         guiGraphics.fill(this.tableX, rowY + this.rowHeight, this.tableX + this.tableWidth, rowY + this.rowHeight + 1, 1078215748);
         this.renderRowData(guiGraphics, record, rowY);
      }
   }

   private void updateButtonStates() {
      this.clearButton.active = this.hasRecords;
   }

   private void updateRecordsState() {
      this.hasRecords = !historyRecords.isEmpty();
   }

   public static class HistoryRecord {
      public final String damageInfo;
      public final String entityName;
      public final boolean isKill;
      public final boolean isPlayer;
      public final long timestamp = System.currentTimeMillis();
      public final String weaponInfo;

      public HistoryRecord(String entityName, String damageInfo, String weaponInfo, boolean isPlayer, boolean isKill) {
         this.entityName = entityName;
         this.damageInfo = damageInfo;
         this.weaponInfo = weaponInfo;
         this.isPlayer = isPlayer;
         this.isKill = isKill;
      }
   }
}
