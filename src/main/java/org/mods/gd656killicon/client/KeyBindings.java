package org.mods.gd656killicon.client;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

public class KeyBindings {
   public static final KeyBinding.Category CATEGORY = KeyBinding.Category.create(Identifier.of("gd656killicon", "main"));
   public static final KeyBinding OPEN_CONFIG = new KeyBinding("key.gd656killicon.open_config", InputUtil.Type.KEYSYM, -1, CATEGORY);
   public static final KeyBinding OPEN_HISTORY = new KeyBinding("key.gd656killicon.open_history", InputUtil.Type.KEYSYM, -1, CATEGORY);
   public static final KeyBinding OPEN_RANKING = new KeyBinding("key.gd656killicon.open_ranking", InputUtil.Type.KEYSYM, -1, CATEGORY);

   private KeyBindings() {
   }
}
