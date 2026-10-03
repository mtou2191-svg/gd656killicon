package org.mods.gd656killicon.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.scoreboard.ScoreAccess;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.ClickEvent.Action;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.mods.gd656killicon.client.KillIconRenderer;
import org.mods.gd656killicon.data.BanListData;
import org.mods.gd656killicon.data.ScoreData;
import org.mods.gd656killicon.data.ScoreExpressionManager;
import org.mods.gd656killicon.data.ScoreboardBindingData;
import org.mods.gd656killicon.network.CardComboKillDurationPacket;
import org.mods.gd656killicon.network.NetworkHandler;

public class ScoreCommand {
   private static final SuggestionProvider<ServerCommandSource> ENTITY_SUGGESTIONS = (context, builder) -> {
      builder.suggest("all");
      return builder.buildFuture();
   };
   private static final Pattern EXPRESSION_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s+\\-*/.]+$");
   private static final Map<String, String> SCORE_TYPE_NAMES = new HashMap<>();
   private static final List<List<Text>> HELP_PAGES = new ArrayList<>();

   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      dispatcher.register(
         CommandManager.literal("gdscore")
            .executes(ScoreCommand::getOwnScore)
            .then(
               CommandManager.literal("help").executes(context -> showHelp(context, 1))
                  .then(
                     CommandManager.argument("page", IntegerArgumentType.integer(1))
                        .executes(context -> showHelp(context, IntegerArgumentType.getInteger(context, "page")))
                  )
            )
            .then(CommandManager.literal("top").executes(ScoreCommand::getTopScores))
            .then(
               CommandManager.literal("get")
                  .requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))
                  .then(
                     CommandManager.argument("target", EntityArgumentType.player())
                        .executes(ScoreCommand::getPlayerScore)
                  )
            )
            .then(
               CommandManager.literal("add")
                  .requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))
                  .then(
                     CommandManager.argument("targets", EntityArgumentType.players())
                        .then(
                           CommandManager.argument("points", IntegerArgumentType.integer(1)).executes(ScoreCommand::addScore)
                        )
                  )
            )
            .then(
               CommandManager.literal("reduce")
                  .requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))
                  .then(
                     CommandManager.argument("targets", EntityArgumentType.players())
                        .then(
                           CommandManager.argument("points", IntegerArgumentType.integer(1)).executes(ScoreCommand::reduceScore)
                        )
                  )
            )
            .then(
               CommandManager.literal("set")
                  .requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))
                  .then(
                     CommandManager.argument("targets", EntityArgumentType.players())
                        .then(CommandManager.argument("points", IntegerArgumentType.integer(0)).executes(ScoreCommand::setScore))
                  )
            )
            .then(
               CommandManager.literal("limit")
                  .requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))
                  .then(
                     CommandManager.argument("maxScore", IntegerArgumentType.integer(1, 2147483646))
                        .executes(ScoreCommand::setScoreLimit)
                  )
            )
            .then(
               CommandManager.literal("ban")
                  .requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))
                  .then(
                     CommandManager.argument("entityId", StringArgumentType.greedyString())
                        .suggests(ENTITY_SUGGESTIONS)
                        .executes(ScoreCommand::banEntity)
                  )
            )
            .then(
               CommandManager.literal("allow")
                  .requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))
                  .then(
                     CommandManager.argument("entityId", StringArgumentType.greedyString())
                        .suggests(ENTITY_SUGGESTIONS)
                        .executes(ScoreCommand::allowEntity)
                  )
            )
            .then(
               CommandManager.literal("banlist")
                  .requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))
                  .executes(ScoreCommand::showBanList)
            )
            .then(
               CommandManager.literal("bonuspoints")
                  .requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))
                  .then(CommandManager.literal("list").executes(ScoreCommand::listScoreExpressions))
                  .then(CommandManager.literal("reset").executes(ScoreCommand::resetScoreExpressions))
                  .then(
                     CommandManager.literal("edit")
                        .then(
                           CommandManager.argument("scoreType", StringArgumentType.string()).suggests((context, builder) -> {
                              ScoreExpressionManager manager = ScoreExpressionManager.get(context.getSource().getServer());
                              Map<String, String> expressions = manager.getAllExpressions();

                              for (String key : expressions.keySet()) {
                                 builder.suggest(key);
                              }

                              return builder.buildFuture();
                           }).then(
                              CommandManager.argument("expression", StringArgumentType.greedyString()).executes(ScoreCommand::editScoreExpression)
                           )
                        )
                  )
            )
            .then(
               CommandManager.literal("rule")
                  .requires(CommandManager.requirePermissionLevel(CommandManager.ADMINS_CHECK))
                  .then(
                     CommandManager.literal("RankingList")
                        .then(CommandManager.argument("enabled", BoolArgumentType.bool()).executes(ScoreCommand::setRankingListRule))
                  )
                  .then(
                     CommandManager.literal("CardComboKillDurationTime")
                        .then(
                           CommandManager.argument("duration", IntegerArgumentType.integer(0, 3600))
                              .executes(ScoreCommand::setCardComboKillDurationTime)
                        )
                  )
            )
            .then(
               CommandManager.literal("scoreboard")
                  .requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))
                  .then(
                     CommandManager.literal("binding")
                        .then(
                           CommandManager.argument("objective", StringArgumentType.string())
                              .then(
                                 CommandManager.argument("displayName", StringArgumentType.greedyString())
                                    .executes(ScoreCommand::bindScoreboard)
                              )
                        )
                  )
                  .then(CommandManager.literal("unbind").executes(ScoreCommand::unbindScoreboard))
            )
            .then(
               CommandManager.literal("cardclear")
                  .requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))
                  .executes(ScoreCommand::clearCardCombo)
            )
      );
   }

   public static void syncScoresToScoreboard(MinecraftServer server) {
      ScoreboardBindingData bindingData = ScoreboardBindingData.get(server);
      if (bindingData.isBound()) {
         ScoreData scoreData = ScoreData.get(server);
         Scoreboard scoreboard = server.getScoreboard();
         ScoreboardObjective objective = scoreboard.getNullableObjective(bindingData.getObjectiveName());
         if (objective == null) {
            objective = scoreboard.addObjective(
               bindingData.getObjectiveName(),
               ScoreboardCriterion.DUMMY,
               Text.literal(bindingData.getDisplayName()),
               ScoreboardCriterion.RenderType.INTEGER,
               false,
               null
            );
         }

         for (Entry<UUID, Integer> entry : scoreData.getAllScores().entrySet()) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());
            if (player != null) {
               ScoreAccess scoreAccess = scoreboard.getOrCreateScore(player, objective);
               scoreAccess.setScore(entry.getValue());
            }
         }
      }
   }

   private static int setScoreLimit(CommandContext<ServerCommandSource> context) {
      int maxScore = IntegerArgumentType.getInteger(context, "maxScore");
      ServerCommandSource source = context.getSource();
      ScoreData scoreData = ScoreData.get(source.getServer());
      scoreData.setScoreLimit(maxScore);
      source.sendFeedback(() -> Text.literal("[六五六] 单次加分上限已设置为: " + maxScore).formatted(Formatting.AQUA), false);
      return 1;
   }

   private static int clearCardCombo(CommandContext<ServerCommandSource> context) {
      ServerCommandSource source = context.getSource();
      if (source.getPlayer() != null) {
         MinecraftClient.getInstance().execute(KillIconRenderer::clearCardCombo);
         source.sendFeedback(() -> Text.literal("[六五六] 已强制清除所有卡牌连杀").formatted(Formatting.AQUA), false);
      } else {
         source.sendFeedback(() -> Text.literal("只有玩家可以执行此指令").formatted(Formatting.GOLD), false);
      }

      return 1;
   }

   private static int setCardComboKillDurationTime(CommandContext<ServerCommandSource> context) {
      int duration = IntegerArgumentType.getInteger(context, "duration");
      ServerCommandSource source = context.getSource();
      ScoreData scoreData = ScoreData.get(source.getServer());
      scoreData.setCardComboKillDurationTime(duration);
      CardComboKillDurationPacket packet = new CardComboKillDurationPacket(duration);
      NetworkHandler.sendToAllPlayers(packet, source.getServer());
      if (duration == 0) {
         source.sendFeedback(() -> Text.literal("[六五六] 卡牌连杀判定时间已设置为永久").formatted(Formatting.AQUA), false);
      } else {
         source.sendFeedback(() -> Text.literal("[六五六] 卡牌连杀判定时间已设置为: " + duration + " 秒").formatted(Formatting.AQUA), false);
      }

      return 1;
   }

   private static int setRankingListRule(CommandContext<ServerCommandSource> context) {
      boolean enabled = BoolArgumentType.getBool(context, "enabled");
      ServerCommandSource source = context.getSource();
      ScoreData scoreData = ScoreData.get(source.getServer());
      scoreData.setRankingListEnabled(enabled);
      if (enabled) {
         source.sendFeedback(() -> Text.literal("[六五六] 榜单已开启").formatted(Formatting.AQUA), false);
      } else {
         source.sendFeedback(() -> Text.literal("[六五六] 榜单已被管理员关闭").formatted(Formatting.GOLD), false);
      }

      return 1;
   }

   private static int bindScoreboard(CommandContext<ServerCommandSource> context) {
      ServerCommandSource source = context.getSource();
      ScoreboardBindingData bindingData = ScoreboardBindingData.get(source.getServer());
      if (bindingData.isBound()) {
         String currentBinding = bindingData.getObjectiveName() + " (" + bindingData.getDisplayName() + ")";
         source.sendFeedback(
            () -> Text.literal(
               "[六五六] 计分板绑定数量已达上限。当前已绑定: " + currentBinding + "。请先使用 /gdscore scoreboard unbind 解绑当前计分板"
            ).formatted(Formatting.GOLD),
            false
         );
         return 0;
      } else {
         String objectiveName = StringArgumentType.getString(context, "objective");
         String displayName = StringArgumentType.getString(context, "displayName");
         if (!isValidObjectiveName(objectiveName)) {
            source.sendFeedback(() -> Text.literal("[六五六] 无效的计分板名称，只能包含字母、数字和下划线").formatted(Formatting.GOLD), false);
            return 0;
         } else {
            bindingData.bind(objectiveName, displayName);
            Scoreboard scoreboard = source.getServer().getScoreboard();
            ScoreboardObjective objective = scoreboard.getNullableObjective(objectiveName);
            if (objective == null) {
               scoreboard.addObjective(
                  objectiveName,
                  ScoreboardCriterion.DUMMY,
                  Text.literal(displayName),
                  ScoreboardCriterion.RenderType.INTEGER,
                  false,
                  null
               );
            }

            source.sendFeedback(() -> Text.literal("[六五六] 已绑定模组分数到计分板: " + objectiveName).formatted(Formatting.AQUA), false);
            syncScoresToScoreboard(source.getServer());
            return 1;
         }
      }
   }

   private static int unbindScoreboard(CommandContext<ServerCommandSource> context) {
      ServerCommandSource source = context.getSource();
      ScoreboardBindingData bindingData = ScoreboardBindingData.get(source.getServer());
      if (!bindingData.isBound()) {
         source.sendFeedback(() -> Text.literal("[六五六] 当前没有绑定任何计分板").formatted(Formatting.GOLD), false);
         return 0;
      } else {
         String objectiveName = bindingData.getObjectiveName();
         bindingData.unbind();
         Scoreboard scoreboard = source.getServer().getScoreboard();
         ScoreboardObjective objective = scoreboard.getNullableObjective(objectiveName);
         if (objective != null) {
            scoreboard.removeObjective(objective);
         }

         source.sendFeedback(() -> Text.literal("[六五六] 已解绑计分板: " + objectiveName).formatted(Formatting.AQUA), false);
         return 1;
      }
   }

   private static int showHelp(CommandContext<ServerCommandSource> context, int page) {
      ServerCommandSource source = context.getSource();
      int totalPages = HELP_PAGES.size();
      if (page < 1) {
         page = 1;
      }

      if (page > totalPages) {
         page = totalPages;
      }

      for (Text line : HELP_PAGES.get(page - 1)) {
         source.sendFeedback(() -> line, false);
      }

      source.sendFeedback(() -> Text.literal(""), false);
      MutableText navigation = Text.literal("");
      if (page > 1) {
         int finalPage = page;
         navigation.append(
            Text.literal("« 上一页")
               .formatted(Formatting.AQUA)
               .styled(
                  style -> style.withClickEvent(new ClickEvent.RunCommand("/gdscore help " + (finalPage - 1)))
                     .withHoverEvent(new HoverEvent.ShowText(Text.literal("点击查看上一页")))
               )
         );
      } else {
         navigation.append(Text.literal("« 上一页").formatted(Formatting.GRAY));
      }

      navigation.append(Text.literal(" | ").formatted(Formatting.GRAY));
      navigation.append(Text.literal(page + "/" + totalPages).formatted(Formatting.DARK_GREEN));
      navigation.append(Text.literal(" | ").formatted(Formatting.GRAY));
      if (page < totalPages) {
         int finalPage1 = page;
         navigation.append(
            Text.literal("下一页 »")
               .formatted(Formatting.AQUA)
               .styled(
                  style -> style.withClickEvent(new ClickEvent.RunCommand("/gdscore help " + (finalPage1 + 1)))
                     .withHoverEvent(new HoverEvent.ShowText(Text.literal("点击查看下一页")))
               )
         );
      } else {
         navigation.append(Text.literal("下一页 »").formatted(Formatting.GRAY));
      }

      source.sendFeedback(() -> navigation, false);
      return 1;
   }

   private static int listScoreExpressions(CommandContext<ServerCommandSource> context) {
      ServerCommandSource source = context.getSource();
      ScoreExpressionManager manager = ScoreExpressionManager.get(source.getServer());
      Map<String, String> expressions = manager.getAllExpressions();
      source.sendFeedback(() -> Text.literal("====== 加分项表达式列表 =====").formatted(Formatting.YELLOW), false);
      source.sendFeedback(() -> Text.literal(""), false);

      for (Entry<String, String> entry : expressions.entrySet()) {
         String scoreType = entry.getKey();
         String displayName = SCORE_TYPE_NAMES.getOrDefault(scoreType, scoreType);
         String expression = entry.getValue();
         MutableText scoreTypeComponent = Text.literal(scoreType).formatted(Formatting.GREEN);
         MutableText fullLine = Text.literal("")
            .append(scoreTypeComponent)
            .append(" (" + displayName + "): ")
            .append(Text.literal(expression).formatted(Formatting.DARK_GREEN));
         source.sendFeedback(() -> fullLine, false);
      }

      return 1;
   }

   private static int resetScoreExpressions(CommandContext<ServerCommandSource> context) {
      ServerCommandSource source = context.getSource();
      ScoreExpressionManager manager = ScoreExpressionManager.get(source.getServer());
      manager.resetToDefaults();
      source.sendFeedback(() -> Text.literal("[六五六] 所有加分项表达式已重置为默认值").formatted(Formatting.AQUA), false);
      return 1;
   }

   private static int editScoreExpression(CommandContext<ServerCommandSource> context) {
      String scoreType = StringArgumentType.getString(context, "scoreType");
      String expression = StringArgumentType.getString(context, "expression");
      ServerCommandSource source = context.getSource();
      ScoreExpressionManager manager = ScoreExpressionManager.get(source.getServer());
      Map<String, String> expressions = manager.getAllExpressions();
      if (!expressions.containsKey(scoreType)) {
         source.sendFeedback(() -> Text.literal("[六五六] 无效的加分项名称").formatted(Formatting.GOLD), false);
         return 0;
      } else if (!validateExpressionFormat(expression)) {
         source.sendFeedback(() -> Text.literal("[六五六] 表达式格式错误，只能包含字母、数字、运算符和空格").formatted(Formatting.GOLD), false);
         return 0;
      } else if (!validateExpression(scoreType, expression)) {
         source.sendFeedback(() -> Text.literal("[六五六] 表达式语法错误").formatted(Formatting.GOLD), false);
         return 0;
      } else {
         manager.setExpression(scoreType, expression);
         source.sendFeedback(() -> Text.literal("[六五六] 加分项 " + scoreType + " 的表达式已更新为: " + expression).formatted(Formatting.AQUA), false);
         return 1;
      }
   }

   private static int banEntity(CommandContext<ServerCommandSource> context) {
      String entityId = StringArgumentType.getString(context, "entityId");
      ServerCommandSource source = context.getSource();
      BanListData banListData = BanListData.get(source.getServer());
      if ("all".equals(entityId)) {
         banListData.clearAll();
         banListData.banEntity("all");
         source.sendFeedback(() -> Text.literal("[六五六] 已禁止所有实体爆出分数"), false);
      } else {
         if (isValidEntityId(entityId)) {
            source.sendFeedback(() -> Text.literal("[六五六] 无效的实体ID: " + entityId), false);
            return 0;
         }

         banListData.banEntity(entityId);
         source.sendFeedback(() -> Text.literal("[六五六] 已禁止实体 " + entityId + " 爆出分数"), false);
      }

      return 1;
   }

   private static int allowEntity(CommandContext<ServerCommandSource> context) {
      String entityId = StringArgumentType.getString(context, "entityId");
      ServerCommandSource source = context.getSource();
      BanListData banListData = BanListData.get(source.getServer());
      if ("all".equals(entityId)) {
         banListData.clearAll();
         source.sendFeedback(() -> Text.literal("[六五六] 已允许所有实体爆出分数"), false);
      } else {
         if (isValidEntityId(entityId)) {
            source.sendFeedback(() -> Text.literal("[六五六] 无效的实体ID: " + entityId), false);
            return 0;
         }

         banListData.unbanEntity(entityId);
         source.sendFeedback(() -> Text.literal("[六五六] 已允许实体 " + entityId + " 爆出分数"), false);
      }

      return 1;
   }

   private static int showBanList(CommandContext<ServerCommandSource> context) {
      ServerCommandSource source = context.getSource();
      BanListData banListData = BanListData.get(source.getServer());
      Collection<String> bannedEntities = banListData.getBannedEntities();
      if (bannedEntities.isEmpty()) {
         source.sendFeedback(() -> Text.literal("[六五六] 当前没有实体被禁止爆出分数"), false);
      } else {
         source.sendFeedback(() -> Text.literal("====== 禁止爆分数的实体列表 ====="), false);

         for (String entityId : bannedEntities) {
            String displayName = getEntityDisplayName(entityId);
            source.sendFeedback(() -> Text.literal("- " + entityId + (displayName != null ? " (" + displayName + ")" : "")), false);
         }
      }

      return 1;
   }

   private static int getOwnScore(CommandContext<ServerCommandSource> context) {
      ServerCommandSource source = context.getSource();
      ServerPlayerEntity player = source.getPlayer();
      if (player != null) {
         int score = ScoreData.get(source.getServer()).getScore(player.getUuid());
         player.sendMessage(Text.literal("[六五六] 您的当前分数: " + score), false);
      }

      return 1;
   }

   private static int getPlayerScore(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerCommandSource source = context.getSource();
      ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "target");
      int score = ScoreData.get(source.getServer()).getScore(target.getUuid());
      source.sendFeedback(() -> Text.literal("[六五六] 玩家 " + target.getName().getString() + " 的分数: " + score), false);
      return 1;
   }

   private static int getTopScores(CommandContext<ServerCommandSource> context) {
      ServerCommandSource source = context.getSource();
      ScoreData scoreData = ScoreData.get(source.getServer());
      if (scoreData.getRankingListEnabled()) {
         source.sendFeedback(() -> Text.literal("[六五六] 榜单已被管理员关闭").formatted(Formatting.RED), false);
         return 0;
      } else {
         Map<UUID, Integer> allScores = scoreData.getAllScores();
         List<Entry<UUID, Integer>> topScores = allScores.entrySet()
            .stream()
            .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
            .limit(10L)
            .toList();
         source.sendFeedback(() -> Text.literal("========== 分数排行榜 =========="), false);
         int rank = 1;

         for (Entry<UUID, Integer> entry : topScores) {
            ServerPlayerEntity player = source.getServer().getPlayerManager().getPlayer(entry.getKey());
            if (player != null) {
               Text line = Text.literal(rank + ". " + player.getName().getString() + ": " + entry.getValue());
               source.sendFeedback(() -> line, false);
               rank++;
            }
         }

         return 1;
      }
   }

   private static int addScore(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerCommandSource source = context.getSource();
      Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(context, "targets");
      int points = IntegerArgumentType.getInteger(context, "points");
      ScoreData scoreData = ScoreData.get(source.getServer());

      for (ServerPlayerEntity player : targets) {
         scoreData.addScore(player.getUuid(), points, source.getServer());
         source.sendFeedback(() -> Text.literal("[六五六] 已给玩家 " + player.getName().getString() + " 增加 " + points + " 分"), false);
      }

      return 1;
   }

   private static int reduceScore(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerCommandSource source = context.getSource();
      Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(context, "targets");
      int points = IntegerArgumentType.getInteger(context, "points");
      ScoreData scoreData = ScoreData.get(source.getServer());

      for (ServerPlayerEntity player : targets) {
         scoreData.reduceScore(player.getUuid(), points, source.getServer());
         source.sendFeedback(() -> Text.literal("[六五六] 已给玩家 " + player.getName().getString() + " 减少 " + points + " 分"), false);
      }

      return 1;
   }

   private static int setScore(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerCommandSource source = context.getSource();
      Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(context, "targets");
      int points = IntegerArgumentType.getInteger(context, "points");
      ScoreData scoreData = ScoreData.get(source.getServer());

      for (ServerPlayerEntity player : targets) {
         scoreData.setScore(player.getUuid(), points, source.getServer());
         source.sendFeedback(() -> Text.literal("[六五六] 已设置玩家 " + player.getName().getString() + " 的分数为 " + points), false);
      }

      return 1;
   }

   private static boolean validateExpressionFormat(String expression) {
      return EXPRESSION_PATTERN.matcher(expression).matches();
   }

   private static boolean validateExpression(String scoreType, String expression) {
      String[] tokens = expression.split(" ");
      if (tokens.length % 2 != 1) {
         return false;
      } else if (scoreType.equals("kill") && expression.contains("killscore")) {
         return false;
      } else {
         Set<String> allowedOperators = Set.of("+", "-", "*", "/");

         for (int i = 1; i < tokens.length; i += 2) {
            if (!allowedOperators.contains(tokens[i])) {
               return false;
            }
         }

         return true;
      }
   }

   private static boolean isValidEntityId(String entityId) {
      try {
         Identifier location = Identifier.tryParse(entityId);
         return location == null || !Registries.ENTITY_TYPE.containsId(location);
      } catch (Exception e) {
         return true;
      }
   }

   private static String getEntityDisplayName(String entityId) {
      try {
         Identifier location = Identifier.tryParse(entityId);
         if (location != null && Registries.ENTITY_TYPE.containsId(location)) {
            EntityType<?> entityType = Registries.ENTITY_TYPE.get(location);
            return entityType.getName().getString();
         }
      } catch (Exception e) {
      }

      return null;
   }

   private static boolean isValidObjectiveName(String name) {
      return name.matches("[a-zA-Z0-9_]+");
   }

   private static void initializeHelpPages() {
      List<Text> page1 = new ArrayList<>();
      page1.add(Text.literal("====== GD656Killicon 帮助 - 第1页/共2页 =====").formatted(Formatting.YELLOW));
      page1.add(Text.literal("").formatted(Formatting.DARK_AQUA));
      page1.add(Text.literal("帮助已经迁移到模组配置菜单中的帮助页面").formatted(Formatting.RED));
      page1.add(Text.literal("").formatted(Formatting.DARK_AQUA));
      page1.add(Text.literal("").formatted(Formatting.DARK_AQUA));
      page1.add(Text.literal("").formatted(Formatting.DARK_AQUA));
      page1.add(Text.literal("").formatted(Formatting.DARK_AQUA));
      page1.add(Text.literal("").formatted(Formatting.DARK_AQUA));
      page1.add(Text.literal("").formatted(Formatting.DARK_AQUA));
      page1.add(Text.literal("").formatted(Formatting.DARK_AQUA));
      page1.add(Text.literal("").formatted(Formatting.DARK_AQUA));
      page1.add(Text.literal(""));
      HELP_PAGES.add(page1);
      List<Text> page2 = new ArrayList<>();
      page2.add(Text.literal("====== GD656Killicon 帮助 - 第2页/共2页 =====").formatted(Formatting.YELLOW));
      page2.add(Text.literal("").formatted(Formatting.DARK_AQUA));
      page2.add(Text.literal("").formatted(Formatting.DARK_AQUA));
      page2.add(Text.literal("").formatted(Formatting.DARK_AQUA));
      page2.add(Text.literal("").formatted(Formatting.DARK_AQUA));
      page2.add(Text.literal("").formatted(Formatting.DARK_AQUA));
      page2.add(Text.literal("").formatted(Formatting.DARK_AQUA));
      page2.add(Text.literal("").formatted(Formatting.DARK_AQUA));
      page2.add(Text.literal("").formatted(Formatting.DARK_AQUA));
      page2.add(Text.literal("").formatted(Formatting.DARK_AQUA));
      page2.add(Text.literal("").formatted(Formatting.DARK_AQUA));
      page2.add(Text.literal(""));
      HELP_PAGES.add(page2);
   }

   static {
      SCORE_TYPE_NAMES.put("kill", "击杀");
      SCORE_TYPE_NAMES.put("critical", "暴击加成");
      SCORE_TYPE_NAMES.put("longrange", "远距离击败");
      SCORE_TYPE_NAMES.put("damage", "造成伤害");
      SCORE_TYPE_NAMES.put("magic", "魔法伤害");
      SCORE_TYPE_NAMES.put("hand", "空手攻击");
      SCORE_TYPE_NAMES.put("assist", "助攻");
      initializeHelpPages();
   }
}
