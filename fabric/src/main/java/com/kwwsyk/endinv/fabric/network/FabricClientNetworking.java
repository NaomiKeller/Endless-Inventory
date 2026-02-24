package com.kwwsyk.endinv.fabric.network;

import com.kwwsyk.endinv.common.network.payloads.ModPacketContext;
import com.kwwsyk.endinv.common.network.payloads.ModPacketPayload;
import com.kwwsyk.endinv.common.network.payloads.SyncedConfig;
import com.kwwsyk.endinv.common.network.payloads.toClient.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class FabricClientNetworking {

    private FabricClientNetworking() {}

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(EndInvContent.TYPE,
                (payload, context) -> context.client().execute(() -> payload.handle(context(context.player()))));
        ClientPlayNetworking.registerGlobalReceiver(EndInvMetadata.TYPE,
                (payload, context) -> context.client().execute(() -> payload.handle(context(context.player()))));
        ClientPlayNetworking.registerGlobalReceiver(ItemPickedUpPayload.TYPE,
                (payload, context) -> context.client().execute(() -> payload.handle(context(context.player()))));
        ClientPlayNetworking.registerGlobalReceiver(SetItemDisplayContentPayload.TYPE,
                (payload, context) -> context.client().execute(() -> payload.handle(context(context.player()))));
        ClientPlayNetworking.registerGlobalReceiver(SetStarredPagePayload.TYPE,
                (payload, context) -> context.client().execute(() -> payload.handle(context(context.player()))));
        ClientPlayNetworking.registerGlobalReceiver(MenuAttachabilityPayload.TYPE,
                (payload, context) -> context.client().execute(() -> payload.handle(context(context.player()))));
        ClientPlayNetworking.registerGlobalReceiver(SyncedConfig.TYPE,
                (payload, context) -> context.client().execute(() -> payload.handle(context(context.player()))));
    }

    private static ModPacketContext context(net.minecraft.world.entity.player.Player player) { return () -> player; }

    public static void sendToServer(ModPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }
}
