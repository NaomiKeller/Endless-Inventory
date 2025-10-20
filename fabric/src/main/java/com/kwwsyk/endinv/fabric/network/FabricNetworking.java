package com.kwwsyk.endinv.fabric.network;

import com.kwwsyk.endinv.common.network.payloads.ModPacketContext;
import com.kwwsyk.endinv.common.network.payloads.ModPacketPayload;
import com.kwwsyk.endinv.common.network.payloads.SyncedConfig;
import com.kwwsyk.endinv.common.network.payloads.toClient.*;
import com.kwwsyk.endinv.common.network.payloads.toServer.*;
import com.kwwsyk.endinv.fabric.network.payloads.JeiTransferRecipePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;

public final class FabricNetworking {
    private FabricNetworking() {}

    public static void init() {
        // Register all payload types/codecs before registering receivers
        var c2s = PayloadTypeRegistry.playC2S();
        c2s.register(ItemClickPayload.TYPE, ItemClickPayload.STREAM_CODEC);
        c2s.register(CreativeItemModPayload.TYPE, CreativeItemModPayload.STREAM_CODEC);
        c2s.register(ItemPageContext.TYPE, ItemPageContext.STREAM_CODEC);
        c2s.register(OpenEndInvPayload.TYPE, OpenEndInvPayload.STREAM_CODEC);
        c2s.register(QuickMoveToPagePayload.TYPE, QuickMoveToPagePayload.STREAM_CODEC);
        c2s.register(StarItemPayload.TYPE, StarItemPayload.STREAM_CODEC);
        c2s.register(ToggleCraftingPayload.TYPE, ToggleCraftingPayload.STREAM_CODEC);
        c2s.register(SyncedConfig.TYPE, SyncedConfig.STREAM_CODEC);
        if (FabricLoader.getInstance().isModLoaded("jei")) {
            c2s.register(JeiTransferRecipePayload.TYPE, JeiTransferRecipePayload.STREAM_CODEC);
        }

        var s2c = PayloadTypeRegistry.playS2C();
        s2c.register(EndInvContent.TYPE, EndInvContent.STREAM_CODEC);
        s2c.register(EndInvMetadata.TYPE, EndInvMetadata.STREAM_CODEC);
        s2c.register(ItemPickedUpPayload.TYPE, ItemPickedUpPayload.STREAM_CODEC);
        s2c.register(SetItemDisplayContentPayload.TYPE, SetItemDisplayContentPayload.STREAM_CODEC);
        s2c.register(SetStarredPagePayload.TYPE, SetStarredPagePayload.STREAM_CODEC);
        s2c.register(MenuAttachabilityPayload.TYPE, MenuAttachabilityPayload.STREAM_CODEC);
        s2c.register(SyncedConfig.TYPE, SyncedConfig.STREAM_CODEC);
    }

    public static void initServerEncodersOnly() { /* no-op on Fabric typed API */ }

    public static void initClient() {
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

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(ItemClickPayload.TYPE,
                (payload, context) -> context.server().execute(() -> payload.handle(context(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(CreativeItemModPayload.TYPE,
                (payload, context) -> context.server().execute(() -> payload.handle(context(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(ItemPageContext.TYPE,
                (payload, context) -> context.server().execute(() -> payload.handle(context(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(OpenEndInvPayload.TYPE,
                (payload, context) -> context.server().execute(() -> payload.handle(context(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(QuickMoveToPagePayload.TYPE,
                (payload, context) -> context.server().execute(() -> payload.handle(context(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(StarItemPayload.TYPE,
                (payload, context) -> context.server().execute(() -> payload.handle(context(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(ToggleCraftingPayload.TYPE,
                (payload, context) -> context.server().execute(() -> payload.handle(context(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(SyncedConfig.TYPE,
                (payload, context) -> context.server().execute(() -> payload.handle(context(context.player()))));
        if (FabricLoader.getInstance().isModLoaded("jei")) {
            ServerPlayNetworking.registerGlobalReceiver(JeiTransferRecipePayload.TYPE,
                    (payload, context) -> context.server().execute(() -> payload.handle(context(context.player()))));
        }
    }

    public static void sendToServer(ModPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    public static void sendToPlayer(ServerPlayer player, ModPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    private static ModPacketContext context(ServerPlayer player) { return () -> player; }
    private static ModPacketContext context(net.minecraft.world.entity.player.Player player) { return () -> player; }
}
