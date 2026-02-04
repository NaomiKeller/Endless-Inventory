package com.kwwsyk.endinv.fabric.network;

import com.kwwsyk.endinv.common.network.payloads.ModPacketContext;
import com.kwwsyk.endinv.common.network.payloads.SyncedConfig;
import com.kwwsyk.endinv.common.network.payloads.toServer.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public final class FabricServerNetworking {

    private FabricServerNetworking() {}

    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(ItemClickPayload.TYPE, (payload, ctx) -> ctx.server().execute(() -> payload.handle(context(ctx.player()))));
        ServerPlayNetworking.registerGlobalReceiver(CreativeItemModPayload.TYPE, (payload, ctx) -> ctx.server().execute(() -> payload.handle(context(ctx.player()))));
        ServerPlayNetworking.registerGlobalReceiver(ItemPageContext.TYPE, (payload, ctx) -> ctx.server().execute(() -> payload.handle(context(ctx.player()))));
        ServerPlayNetworking.registerGlobalReceiver(OpenEndInvPayload.TYPE, (payload, ctx) -> ctx.server().execute(() -> payload.handle(context(ctx.player()))));
        ServerPlayNetworking.registerGlobalReceiver(QuickMoveToPagePayload.TYPE, (payload, ctx) -> ctx.server().execute(() -> payload.handle(context(ctx.player()))));
        ServerPlayNetworking.registerGlobalReceiver(StarItemPayload.TYPE, (payload, ctx) -> ctx.server().execute(() -> payload.handle(context(ctx.player()))));
        ServerPlayNetworking.registerGlobalReceiver(ToggleCraftingPayload.TYPE, (payload, ctx) -> ctx.server().execute(() -> payload.handle(context(ctx.player()))));
        ServerPlayNetworking.registerGlobalReceiver(SyncedConfig.TYPE, (payload, ctx) -> ctx.server().execute(() -> payload.handle(context(ctx.player()))));
        ServerPlayNetworking.registerGlobalReceiver(BulkQuickMoveFromPagePayload.TYPE, (payload, ctx) -> ctx.server().execute(() -> payload.handle(context(ctx.player()))));
        ServerPlayNetworking.registerGlobalReceiver(com.kwwsyk.endinv.fabric.network.payloads.JeiTransferRecipePayload.TYPE,
                (payload, ctx) -> ctx.server().execute(() -> payload.handle(context(ctx.player()))));
    }


    private static ModPacketContext context(ServerPlayer player) {
        return () -> player;
    }
}
