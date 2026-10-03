package org.mods.gd656killicon.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

public class ScoreboardBindingData extends PersistentState {
   private static final String DATA_NAME = "gd656killicon_scoreboard_binding";
   public static final PersistentStateType<ScoreboardBindingData> TYPE = new PersistentStateType<>(
      DATA_NAME, ScoreboardBindingData::new, createCodec(), DataFixTypes.LEVEL
   );
   private String displayName;
   private String objectiveName;

   public ScoreboardBindingData() {
   }

   private record Serialized(Optional<String> objectiveName, Optional<String> displayName) {
   }

   private static Codec<ScoreboardBindingData> createCodec() {
      Codec<Serialized> serializedCodec = RecordCodecBuilder.create(instance -> instance.group(
         Codec.STRING.optionalFieldOf("objectiveName").forGetter(Serialized::objectiveName),
         Codec.STRING.optionalFieldOf("displayName").forGetter(Serialized::displayName)
      ).apply(instance, Serialized::new));
      return serializedCodec.xmap(serialized -> {
         ScoreboardBindingData data = new ScoreboardBindingData();
         serialized.objectiveName().ifPresent(name -> data.objectiveName = name);
         serialized.displayName().ifPresent(name -> data.displayName = name);
         return data;
      }, data -> new Serialized(Optional.ofNullable(data.objectiveName), Optional.ofNullable(data.displayName)));
   }

   public static ScoreboardBindingData get(MinecraftServer server) {
      return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE);
   }

   public boolean isBound() {
      return this.objectiveName != null && !this.objectiveName.isEmpty() && this.displayName != null && !this.displayName.isEmpty();
   }

   public String getObjectiveName() {
      return this.objectiveName;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public void bind(String objectiveName, String displayName) {
      this.objectiveName = objectiveName;
      this.displayName = displayName;
      this.markDirty();
   }

   public void unbind() {
      this.objectiveName = null;
      this.displayName = null;
      this.markDirty();
   }
}
