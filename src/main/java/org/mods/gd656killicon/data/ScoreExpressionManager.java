package org.mods.gd656killicon.data;

import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

public class ScoreExpressionManager extends PersistentState {
   private static final String DATA_NAME = "gd656killicon_score_expressions";
   public static final PersistentStateType<ScoreExpressionManager> TYPE = new PersistentStateType<>(
      DATA_NAME, ScoreExpressionManager::new, createCodec(), DataFixTypes.LEVEL
   );
   private final Map<String, String> expressions = new HashMap<>();

   public ScoreExpressionManager() {
      this.resetToDefaults();
   }

   private static Codec<ScoreExpressionManager> createCodec() {
      return Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("expressions", Map.of()).xmap(map -> {
         ScoreExpressionManager manager = new ScoreExpressionManager();
         manager.expressions.clear();
         manager.expressions.putAll(map);
         if (manager.expressions.isEmpty()) {
            manager.resetToDefaults();
         }

         return manager;
      }, manager -> new HashMap<>(manager.expressions)).codec();
   }

   public static ScoreExpressionManager get(MinecraftServer server) {
      return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE);
   }

   public void resetToDefaults() {
      this.expressions.clear();
      this.expressions.put("kill", "health * 5");
      this.expressions.put("critical", "killscore * 0.25");
      this.expressions.put("longrange", "distance");
      this.expressions.put("damage", "damage");
      this.expressions.put("magic", "damage");
      this.expressions.put("hand", "damage");
      this.expressions.put("assist", "damagedealt * 5");
      this.expressions.put("combobonus", "combo * 8");
      this.markDirty();
   }

   public String getExpression(String scoreType) {
      return this.expressions.getOrDefault(scoreType, "");
   }

   public void setExpression(String scoreType, String expression) {
      this.expressions.put(scoreType, expression);
      this.markDirty();
   }

   public Map<String, String> getAllExpressions() {
      return new HashMap<>(this.expressions);
   }
}
