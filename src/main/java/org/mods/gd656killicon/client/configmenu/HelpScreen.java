package org.mods.gd656killicon.client.configmenu;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.mods.gd656killicon.client.RenderHelper;

public class HelpScreen extends Screen {
   private final Screen parent;
   private static final int CATEGORY_BUTTON_HEIGHT = 25;
   private static final int CATEGORY_BUTTON_WIDTH = 120;
   private static final int CONTENT_MARGIN = 15;
   private static final int SEARCH_HEIGHT = 30;
   private static final int TITLE_HEIGHT = 40;
   private List<HelpScreen.CategoryButton> categoryButtons;
   private HelpScreen.HelpContentArea contentArea;
   private HelpScreen.HelpCategory currentCategory = HelpScreen.HelpCategory.BASIC;
   private TextFieldWidget searchBox;

   public HelpScreen(Screen parent) {
      super(Text.literal("帮助中心"));
      this.parent = parent;
   }

   protected void init() {
      super.init();
      this.initCategoryButtons();
      this.initSearchBox();
      this.initContentArea();
      this.initBackButton();
      this.updateContent();
   }

   public void close() {
      if (this.client != null) {
         this.client.setScreen(this.parent);
      }
   }

   public void render(@NotNull DrawContext guiGraphics, int mouseX, int mouseY, float partialTick) {
      guiGraphics.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, -1);
      this.renderCategoryButtons(guiGraphics, mouseX, mouseY);
      this.contentArea.render(guiGraphics);
      if (this.searchBox.getText().isEmpty() && !this.searchBox.isFocused()) {
         guiGraphics.drawText(
            this.textRenderer,
            Text.literal("搜索命令或功能...").formatted(Formatting.GRAY),
            this.searchBox.getX() + 4,
            this.searchBox.getY() + (this.searchBox.getHeight() - 8) / 2,
            8421504,
            false
         );
      }

      super.render(guiGraphics, mouseX, mouseY, partialTick);
   }

   public boolean mouseClicked(Click click, boolean doubled) {
      double mouseX = click.x();
      double mouseY = click.y();
      int button = click.button();
      for (HelpScreen.CategoryButton catButton : this.categoryButtons) {
         if (catButton.contains(mouseX, mouseY)) {
            this.currentCategory = catButton.category;
            this.updateContent();
            return true;
         }
      }

      return this.contentArea.contains(mouseX, mouseY) && this.contentArea.handleMouseClick(mouseX) ? true : super.mouseClicked(click, doubled);
   }

   public boolean mouseDragged(Click click, double dragX, double dragY) {
      return this.contentArea.handleMouseDrag(dragY) ? true : super.mouseDragged(click, dragX, dragY);
   }

   public boolean mouseReleased(Click click) {
      this.contentArea.handleMouseRelease();
      return super.mouseReleased(click);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      if (this.contentArea.contains(mouseX, mouseY)) {
         this.contentArea.scroll(verticalAmount);
         return true;
      } else {
         return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
      }
   }

   private void addAdvancedFeatures(List<Text> content) {
      content.add(Text.literal("高级功能").formatted(Formatting.YELLOW, Formatting.BOLD));
      content.add(Text.literal(""));
      content.add(Text.literal("帮助系统").formatted(Formatting.WHITE, Formatting.BOLD));
      this.addCommandEntry(content, "/gdscore help", "查看命令帮助", "权限等级: 0", "示例: /gdscore help");
      this.addCommandEntry(content, "/gdscore help <页码>", "查看指定页码的帮助", "权限等级: 0", "示例: /gdscore help 2");
      content.add(Text.literal(""));
      content.add(Text.literal("权限说明").formatted(Formatting.WHITE, Formatting.BOLD));
      content.add(Text.literal("0: 所有玩家").formatted(Formatting.GRAY));
      content.add(Text.literal("2: 操作员/管理员").formatted(Formatting.GRAY));
      content.add(Text.literal("3: 服务器管理员").formatted(Formatting.GRAY));
      content.add(Text.literal(""));
      content.add(Text.literal("模组信息").formatted(Formatting.WHITE, Formatting.BOLD));
      content.add(Text.literal("• 模组版本: 1.0.0 公测版 RC5 *Fabric*").formatted(Formatting.GRAY));
      content.add(Text.literal("• 支持Minecraft: 1.20.1").formatted(Formatting.GRAY));
      content.add(Text.literal("• 开发者: Minecraft_GD656").formatted(Formatting.GRAY));
   }

   private void addBasicCommands(List<Text> content) {
      content.add(Text.literal("基础查询命令").formatted(Formatting.YELLOW, Formatting.BOLD));
      content.add(Text.literal(""));
      this.addCommandEntry(content, "/gdscore", "查看自己的当前分数", "权限等级: 0", "示例: /gdscore");
      this.addCommandEntry(content, "/gdscore top", "查看服务器分数排行榜", "权限等级: 0", "示例: /gdscore top");
      this.addCommandEntry(content, "/gdscore get <玩家>", "查看指定玩家的分数", "权限等级: 2", "示例: /gdscore get Steve");
      this.addCommandEntry(content, "/gdscore cardclear", "清除当前所有卡牌", "权限等级: 0", "示例: /gdscore cardclear");
      content.add(Text.literal(""));
      content.add(Text.literal("提示: 所有玩家都可以查看自己的分数和排行榜").formatted(Formatting.GRAY));
   }

   private void addBonusSystemContent(List<Text> content) {
      content.add(Text.literal("加分项系统").formatted(Formatting.YELLOW, Formatting.BOLD));
      content.add(Text.literal(""));
      content.add(Text.literal("管理命令").formatted(Formatting.WHITE, Formatting.BOLD));
      this.addCommandEntry(content, "/gdscore bonuspoints list", "列出所有加分项表达式", "权限等级: 2", "");
      this.addCommandEntry(content, "/gdscore bonuspoints reset", "重置所有加分项表达式为默认值", "权限等级: 2", "");
      this.addCommandEntry(content, "/gdscore bonuspoints edit <加分项> <表达式>", "编辑加分项的表达式", "权限等级: 2", "示例: /gdscore bonuspoints edit kill health * 5");
      content.add(Text.literal(""));
      content.add(Text.literal("可用变量").formatted(Formatting.WHITE, Formatting.BOLD));
      this.addVariableEntry(content, "health", "目标生命值");
      this.addVariableEntry(content, "distance", "攻击距离");
      this.addVariableEntry(content, "damage", "单次伤害");
      this.addVariableEntry(content, "damagedealt", "总伤害");
      this.addVariableEntry(content, "combo", "连杀数");
      this.addVariableEntry(content, "killscore", "击杀基础分数");
      content.add(Text.literal(""));
      content.add(Text.literal("运算符: +  -  *  /").formatted(Formatting.GRAY));
   }

   private void addCommandEntry(List<Text> content, String command, String description, String permission, String example) {
      content.add(Text.literal(command).formatted(Formatting.AQUA));
      content.add(Text.literal("  " + description).formatted(Formatting.WHITE));
      content.add(Text.literal("  " + permission).formatted(Formatting.DARK_GRAY));
      if (!example.isEmpty()) {
         content.add(Text.literal("  " + example).formatted(Formatting.DARK_GREEN));
      }

      content.add(Text.literal(""));
   }

   private void addEntityManagementCommands(List<Text> content) {
      content.add(Text.literal("实体管理命令").formatted(Formatting.YELLOW, Formatting.BOLD));
      content.add(Text.literal(""));
      this.addCommandEntry(content, "/gdscore ban <实体ID>", "禁止实体爆出分数", "权限等级: 2", "示例: /gdscore ban minecraft:zombie");
      this.addCommandEntry(content, "/gdscore allow <实体ID>", "允许实体爆出分数", "权限等级: 2", "示例: /gdscore allow minecraft:skeleton");
      this.addCommandEntry(content, "/gdscore banlist", "查看禁止爆出分数的实体列表", "权限等级: 2", "示例: /gdscore banlist");
      this.addCommandEntry(content, "/gdscore ban all", "禁止所有实体爆出分数", "权限等级: 2", "特殊命令");
      this.addCommandEntry(content, "/gdscore allow all", "允许所有实体爆出分数", "权限等级: 2", "特殊命令");
      content.add(Text.literal(""));
      content.add(Text.literal("实体ID格式: minecraft:实体名称").formatted(Formatting.GRAY));
   }

   private void addRuleCommands(List<Text> content) {
      content.add(Text.literal("规则设置").formatted(Formatting.YELLOW, Formatting.BOLD));
      content.add(Text.literal(""));
      this.addCommandEntry(content, "/gdscore rule RankingList <true/false>", "开启或关闭排行榜功能", "权限等级: 3", "示例: /gdscore rule RankingList false");
      content.add(Text.literal(""));
      content.add(Text.literal("功能说明").formatted(Formatting.WHITE));
      content.add(Text.literal("• 控制服务器排行榜的显示状态").formatted(Formatting.GRAY));
      content.add(Text.literal("• 关闭后玩家无法查看/top排行榜").formatted(Formatting.GRAY));
      content.add(Text.literal("• 不影响实际分数计算").formatted(Formatting.GRAY));
      content.add(Text.literal(""));
      content.add(Text.literal("使用场景").formatted(Formatting.WHITE));
      content.add(Text.literal("• 玩家过多避免过多访问造成服务器带宽不足").formatted(Formatting.GRAY));
      content.add(Text.literal(""));
      this.addCommandEntry(
         content, "/gdscore rule CardComboKillDurationTime < x >", "修改卡牌连杀显示判定时间", "权限等级: 3", "示例: /gdscore rule CardComboKillDurationTime 60（60秒）"
      );
      content.add(Text.literal(""));
      content.add(Text.literal("功能说明").formatted(Formatting.WHITE));
      content.add(Text.literal("• 默认值为60秒即一分钟").formatted(Formatting.GRAY));
      content.add(Text.literal("• 全服玩家统一").formatted(Formatting.GRAY));
      content.add(Text.literal("• 不影响实际分数计算").formatted(Formatting.GRAY));
   }

   private void addScoreManagementCommands(List<Text> content) {
      content.add(Text.literal("分数管理命令").formatted(Formatting.YELLOW, Formatting.BOLD));
      content.add(Text.literal(""));
      this.addCommandEntry(content, "/gdscore add <玩家> <分数>", "给指定玩家增加分数", "权限等级: 2", "示例: /gdscore add Steve 100");
      this.addCommandEntry(content, "/gdscore reduce <玩家> <分数>", "给指定玩家减少分数", "权限等级: 2", "示例: /gdscore reduce Steve 50");
      this.addCommandEntry(content, "/gdscore set <玩家> <分数>", "设置指定玩家的分数", "权限等级: 2", "示例: /gdscore set Steve 1000");
      content.add(Text.literal(""));
      content.add(Text.literal("注意: 这些命令需要操作员权限").formatted(Formatting.RED));
   }

   private void addScoreboardCommands(List<Text> content) {
      content.add(Text.literal("计分板集成").formatted(Formatting.YELLOW, Formatting.BOLD));
      content.add(Text.literal(""));
      this.addCommandEntry(content, "/gdscore scoreboard binding <计分板ID> <显示名称>", "将模组分数绑定到原版计分板", "权限等级: 2", "示例: /gdscore scoreboard binding killpoints 击杀分数");
      this.addCommandEntry(content, "/gdscore scoreboard unbind", "解除计分板绑定", "权限等级: 2", "示例: /gdscore scoreboard unbind");
      content.add(Text.literal(""));
      content.add(Text.literal("功能说明").formatted(Formatting.WHITE));
      content.add(Text.literal("• 自动同步玩家分数到原版计分板").formatted(Formatting.GRAY));
      content.add(Text.literal("• 支持计分板显示名称自定义").formatted(Formatting.GRAY));
      content.add(Text.literal("• 实时更新分数变化").formatted(Formatting.GRAY));
   }

   private void addVariableEntry(List<Text> content, String variable, String description) {
      content.add(Text.literal("  " + variable).formatted(Formatting.GREEN));
      content.add(Text.literal("    - " + description).formatted(Formatting.GRAY));
   }

   private List<Text> generateCategoryContent(HelpScreen.HelpCategory category) {
      List<Text> content = new ArrayList<>();
      content.add(Text.literal(category.displayName + "指南").formatted(category.color, Formatting.BOLD));
      content.add(Text.literal(""));
      switch (category) {
         case ADVANCED:
            this.addAdvancedFeatures(content);
            break;
         case BASIC:
            this.addBasicCommands(content);
            break;
         case BONUS_SYSTEM:
            this.addBonusSystemContent(content);
            break;
         case ENTITY_MANAGEMENT:
            this.addEntityManagementCommands(content);
            break;
         case RULE:
            this.addRuleCommands(content);
            break;
         case SCORE_MANAGEMENT:
            this.addScoreManagementCommands(content);
            break;
         case SCOREBOARD:
            this.addScoreboardCommands(content);
            break;
         default:
            content.add(Text.literal("此分类内容正在开发中...").formatted(Formatting.YELLOW));
      }

      return content;
   }

   private void initBackButton() {
      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, button -> {
         if (this.client != null) {
            this.client.setScreen(this.parent);
         }
      }).dimensions(this.width - 80 - 15, 15, 80, 20).build());
   }

   private void initCategoryButtons() {
      this.categoryButtons = new ArrayList<>();
      int startY = 80;

      for (HelpScreen.HelpCategory category : HelpScreen.HelpCategory.values()) {
         this.categoryButtons.add(new HelpScreen.CategoryButton(15, startY, 120, 25, category));
         startY += 30;
      }
   }

   private void initContentArea() {
      int contentX = 150;
      int contentY = 80;
      int contentWidth = this.width - contentX - 15;
      int contentHeight = this.height - contentY - 50;
      this.contentArea = new HelpScreen.HelpContentArea(contentX, contentY, contentWidth, contentHeight);
   }

   private void initSearchBox() {
      int searchWidth = this.width - 120 - 45;
      this.searchBox = new TextFieldWidget(this.textRenderer, 150, 40, searchWidth, 30, Text.literal("搜索命令或功能..."));
      this.searchBox.setMaxLength(50);
      this.searchBox.setChangedListener(this::onSearchTextChanged);
      this.addDrawableChild(this.searchBox);
   }

   private void onSearchTextChanged(String searchText) {
      if (searchText.isEmpty()) {
         this.updateContent();
      } else {
         this.performSearch(searchText);
      }
   }

   private void performSearch(String searchText) {
      List<Text> searchResults = new ArrayList<>();
      String lowerSearch = searchText.toLowerCase();

      for (HelpScreen.HelpCategory category : HelpScreen.HelpCategory.values()) {
         for (Text line : this.generateCategoryContent(category)) {
            if (line.getString().toLowerCase().contains(lowerSearch)) {
               if (searchResults.isEmpty()) {
                  searchResults.add(Text.literal("搜索结果: \"" + searchText + "\"").formatted(Formatting.GOLD));
                  searchResults.add(Text.literal(""));
               }

               searchResults.add(line);
            }
         }
      }

      if (searchResults.isEmpty()) {
         searchResults.add(Text.literal("未找到包含 \"" + searchText + "\" 的内容").formatted(Formatting.RED));
         searchResults.add(Text.literal("请尝试其他关键词").formatted(Formatting.GRAY));
      }

      this.contentArea.setContent(searchResults);
   }

   private void renderCategoryButtons(DrawContext guiGraphics, int mouseX, int mouseY) {
      for (HelpScreen.CategoryButton button : this.categoryButtons) {
         button.render(guiGraphics, mouseX, mouseY, button.category == this.currentCategory);
      }
   }

   private void updateContent() {
      this.contentArea.setContent(this.generateCategoryContent(this.currentCategory));
   }

   private class CategoryButton {
      public final HelpScreen.HelpCategory category;
      public final int height;
      public final int width;
      public final int x;
      public final int y;

      public CategoryButton(int x, int y, int width, int height, HelpScreen.HelpCategory category) {
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height;
         this.category = category;
      }

      public boolean contains(double mouseX, double mouseY) {
         return mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.height;
      }

      public void render(DrawContext guiGraphics, int mouseX, int mouseY, boolean isSelected) {
         int fillColor = isSelected ? -12566464 : (this.contains(mouseX, mouseY) ? -13619152 : -14671840);
         int borderColor = isSelected ? -1 : -8355712;
         guiGraphics.fill(this.x, this.y, this.x + this.width, this.y + this.height, fillColor);
         RenderHelper.drawBorder(guiGraphics, this.x, this.y, this.width, this.height, borderColor);
         Integer categoryColor = this.category.color.getColorValue();
         int textColor = isSelected ? (categoryColor == null ? -1 : categoryColor) : -1;
         guiGraphics.drawCenteredTextWithShadow(HelpScreen.this.textRenderer, this.category.displayName, this.x + this.width / 2, this.y + (this.height - 8) / 2, textColor);
      }
   }

   private static enum HelpCategory {
      ADVANCED("高级功能", Formatting.DARK_PURPLE),
      BASIC("基础指令", Formatting.RED),
      BONUS_SYSTEM("加分系统", Formatting.GREEN),
      ENTITY_MANAGEMENT("实体管理", Formatting.YELLOW),
      RULE("规则设置", Formatting.BLUE),
      SCORE_MANAGEMENT("分数管理", Formatting.GOLD),
      SCOREBOARD("计分板", Formatting.DARK_AQUA);

      public final String displayName;
      public final Formatting color;

      private HelpCategory(String displayName, Formatting color) {
         this.displayName = displayName;
         this.color = color;
      }
   }

   private class HelpContentArea {
      private final int height;
      private final int width;
      private final int x;
      private final int y;
      private List<Text> content;
      private boolean isScrolling;
      private double maxScroll;
      private double scrollOffset;

      public HelpContentArea(int x, int y, int width, int height) {
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height;
         this.content = new ArrayList<>();
         this.scrollOffset = 0.0;
         this.maxScroll = 0.0;
      }

      private void calculateMaxScroll() {
         int totalHeight = this.content.size() * (9 + 2) + 20;
         this.maxScroll = Math.max(0, totalHeight - this.height);
      }

      public boolean contains(double mouseX, double mouseY) {
         return mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.height;
      }

      public boolean handleMouseClick(double mouseX) {
         int scrollBarX = this.x + this.width - 8;
         if (mouseX >= scrollBarX && mouseX <= scrollBarX + 8) {
            this.isScrolling = true;
            return true;
         } else {
            return false;
         }
      }

      public boolean handleMouseDrag(double deltaY) {
         if (this.isScrolling) {
            double dragRatio = deltaY / (this.height - 20);
            this.scroll(dragRatio * this.maxScroll * 2.0);
            return true;
         } else {
            return false;
         }
      }

      public void handleMouseRelease() {
         this.isScrolling = false;
      }

      public void render(DrawContext guiGraphics) {
         guiGraphics.fill(this.x, this.y, this.x + this.width, this.y + this.height, Integer.MIN_VALUE);
         RenderHelper.drawBorder(guiGraphics, this.x, this.y, this.width, this.height, -12566464);
         guiGraphics.enableScissor(this.x, this.y, this.x + this.width, this.y + this.height);
         int textY = this.y + 10 - (int)this.scrollOffset;

         for (Text line : this.content) {
            if (textY + 9 >= this.y && textY <= this.y + this.height) {
               guiGraphics.drawText(HelpScreen.this.textRenderer, line, this.x + 10, textY, -1, false);
            }

            textY += 9 + 2;
         }

         guiGraphics.disableScissor();
         if (this.maxScroll > 0.0) {
            this.renderScrollBar(guiGraphics);
         }
      }

      private void renderScrollBar(DrawContext guiGraphics) {
         int scrollBarWidth = 6;
         int scrollBarX = this.x + this.width - scrollBarWidth - 2;
         double visibleRatio = this.height / (this.height + this.maxScroll);
         int scrollBarHeight = Math.max(20, Math.min((int)(this.height * visibleRatio), this.height - 4));
         int scrollBarY = this.y + 2 + (int)((this.height - 4 - scrollBarHeight) * (this.scrollOffset / this.maxScroll));
         guiGraphics.fill(scrollBarX, this.y + 1, scrollBarX + scrollBarWidth, this.y + this.height - 1, -2143272896);
         guiGraphics.fill(scrollBarX, scrollBarY, scrollBarX + scrollBarWidth, scrollBarY + scrollBarHeight, -8355712);
         RenderHelper.drawBorder(guiGraphics, scrollBarX, scrollBarY, scrollBarWidth, scrollBarHeight, -4144960);
      }

      public void scroll(double delta) {
         this.scrollOffset = Math.max(0.0, Math.min(this.scrollOffset - delta * 10.0, this.maxScroll));
      }

      public void setContent(List<Text> content) {
         this.content = content;
         this.scrollOffset = 0.0;
         this.calculateMaxScroll();
      }
   }
}
