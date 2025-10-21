package com.kwwsyk.endinv.forge.mixin;

import com.kwwsyk.endinv.common.network.payloads.ModPacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientCustomPayloadMixin {

    @Inject(method = "handleCustomPayload", at = @At("HEAD"))
    private void endinv$handleCustomPayload(ClientboundCustomPayloadPacket packet, CallbackInfo ci) {
        CustomPacketPayload pl = packet.payload();
        if (pl instanceof ModPacketPayload mod) {
            mod.handle(() -> Minecraft.getInstance().player);
        }
    }
}

