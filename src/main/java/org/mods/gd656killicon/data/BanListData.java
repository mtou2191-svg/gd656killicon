package org.mods.gd656killicon.data;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

public class BanListData extends PersistentState {
   private static final String DATA_NAME = "gd656killicon_banlist";
   public static final PersistentStateType<BanListData> TYPE = new PersistentStateType<>(DATA_NAME, BanListData::new, createCodec(), DataFixTypes.LEVEL);
   private final Set<String> bannedEntities = new HashSet<>();

   public BanListData() {
   }

   private static Codec<BanListData> createCodec() {
      return Codec.STRING.listOf().optionalFieldOf("bannedEntities", List.of()).xmap(list -> {
         BanListData data = new BanListData();
         data.bannedEntities.addAll(list);
         return data;
      }, data -> new ArrayList<>(data.bannedEntities)).codec();
   }

   public static BanListData get() {
      MinecraftServer server = getCurrentServer();
      if (server == null) {
         return new BanListData();
      } else {
         return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE);
      }
   }

   public boolean isBanned(String entityId) {
      return this.bannedEntities.contains(entityId) || this.bannedEntities.contains("all");
   }

   public void banEntity(String entityId) {
      if ("all".equals(entityId)) {
         this.bannedEntities.clear();
      }

      this.bannedEntities.add(entityId);
      this.markDirty();
   }

   public void unbanEntity(String entityId) {
      if ("all".equals(entityId)) {
         this.bannedEntities.clear();
      } else {
         this.bannedEntities.remove(entityId);
      }

      this.markDirty();
   }

   public Set<String> getBannedEntities() {
      return new HashSet<>(this.bannedEntities);
   }

   public void clearAll() {
      this.bannedEntities.clear();
      this.markDirty();
   }

   private static MinecraftServer getCurrentServer() {
      return null;
   }

   public static BanListData get(MinecraftServer server) {
      if (server == null) {
         return new BanListData();
      } else {
         return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE);
      }
   }
}
