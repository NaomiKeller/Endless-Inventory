package com.kwwsyk.endinv.forge.mixin;

import com.kwwsyk.endinv.common.network.payloads.ModPacketPayload;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerCustomPayloadMixin {

    @Shadow public ServerPlayer player;

    @Inject(method = "handleCustomPayload", at = @At("HEAD"))
    private void endinv$handleCustomPayload(ServerboundCustomPayloadPacket packet, CallbackInfo ci) {
        CustomPacketPayload pl = packet.payload();
        if (pl instanceof ModPacketPayload mod) {
            mod.handle(() -> this.player);
        }
    }
}

