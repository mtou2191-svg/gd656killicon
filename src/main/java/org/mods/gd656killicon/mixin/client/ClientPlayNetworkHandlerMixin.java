package org.mods.gd656killicon.mixin.client;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import org.mods.gd656killicon.client.ClientKillTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
   @Inject(method = "onEntityStatus", at = @At("TAIL"))
   private void gd656$onEntityStatus(EntityStatusS2CPacket packet, CallbackInfo ci) {
      ClientKillTracker.onEntityStatus(packet);
   }
}
