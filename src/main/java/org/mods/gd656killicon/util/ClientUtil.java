package org.mods.gd656killicon.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public class ClientUtil {
   public static boolean isClient() {
      return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
   }
}
