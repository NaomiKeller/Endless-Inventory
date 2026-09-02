package com.kwwsyk.endinv.fabric.network;

import com.kwwsyk.endinv.common.AbstractModInitializer;
import com.kwwsyk.endinv.common.network.payloads.ModPacketContext;
import com.kwwsyk.endinv.common.network.payloads.ModPacketPayload;
import com.kwwsyk.endinv.common.network.payloads.SyncedConfig;
import com.kwwsyk.endinv.common.network.payloads.toClient.*;
import com.kwwsyk.endinv.common.network.payloads.toServer.*;
import com.kwwsyk.endinv.fabric.network.payloads.JeiTransferRecipePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class FabricNetworking {

    private static final Map<Class<? extends ModPacketPayload>, PayloadRegistration<? extends ModPacketPayload>> CLIENTBOUND = new HashMap<>();
    private static final Map<Class<? extends ModPacketPayload>, PayloadRegistration<? extends ModPacketPayload>> SERVERBOUND = new HashMap<>();
    private static final List<PayloadRegistration<? extends ModPacketPayload>> CLIENTBOUND_REGISTRATIONS = new ArrayList<>();

    private FabricNetworking() {
    }

    public static void init() {
        registerClientbound(EndInvContent.class, EndInvContent::encode, EndInvContent::decode, "endinv_content");
        registerClientbound(EndInvMetadata.class, EndInvMetadata::encode, EndInvMetadata::decode, "endinv_meta");
        registerClientbound(ItemPickedUpPayload.class, ItemPickedUpPayload::encode, ItemPickedUpPayload::decode, "auto_picked");
        registerClientbound(SetItemDisplayContentPayload.class, SetItemDisplayContentPayload::encode, SetItemDisplayContentPayload::decode, "itemdisplay_content");
        registerClientbound(SetStarredPagePayload.class, SetStarredPagePayload::encode, SetStarredPagePayload::decode, "starred_item");
        registerClientbound(MenuAttachabilityPayload.class, MenuAttachabilityPayload::encode, MenuAttachabilityPayload::decode, "menu_attachability");
        registerClientbound(SyncedConfig.class, SyncedConfig::encode, SyncedConfig::decode, SyncedConfig.DEFAULT.id());

        registerServerbound(ItemClickPayload.class, ItemClickPayload::encode, ItemClickPayload::decode, "item_click");
        registerServerbound(CreativeItemModPayload.class, CreativeItemModPayload::encode, CreativeItemModPayload::decode, "item_modify");
        registerServerbound(ItemPageContext.class, ItemPageContext::encode, ItemPageContext::decode, "page_context");
        registerServerbound(OpenEndInvPayload.class, OpenEndInvPayload::encode, OpenEndInvPayload::decode, "open_endinv");
        registerServerbound(QuickMoveToPagePayload.class, QuickMoveToPagePayload::encode, QuickMoveToPagePayload::decode, "quick_move_page");
        registerServerbound(BulkQuickMoveFromPagePayload.class, BulkQuickMoveFromPagePayload::encode, BulkQuickMoveFromPagePayload::decode, "bulk_quick_move");
        registerServerbound(StarItemPayload.class, StarItemPayload::encode, StarItemPayload::decode, "star_item");
        registerServerbound(ToggleCraftingPayload.class, ToggleCraftingPayload::encode, ToggleCraftingPayload::decode, "toggle_crafting");
        registerServerbound(SyncedConfig.class, SyncedConfig::encode, SyncedConfig::decode, SyncedConfig.DEFAULT.id());
        if (FabricLoader.getInstance().isModLoaded("jei")) {
            registerServerbound(JeiTransferRecipePayload.class, JeiTransferRecipePayload::encode, JeiTransferRecipePayload::decode, "jei_transfer_recipe");
        }
    }

    // Populate clientbound encoder/decoder map on the server without registering receivers
    public static void initServerEncodersOnly() {
        registerClientbound(EndInvContent.class, EndInvContent::encode, EndInvContent::decode, "endinv_content");
        registerClientbound(EndInvMetadata.class, EndInvMetadata::encode, EndInvMetadata::decode, "endinv_meta");
        registerClientbound(ItemPickedUpPayload.class, ItemPickedUpPayload::encode, ItemPickedUpPayload::decode, "auto_picked");
        registerClientbound(SetItemDisplayContentPayload.class, SetItemDisplayContentPayload::encode, SetItemDisplayContentPayload::decode, "itemdisplay_content");
        registerClientbound(SetStarredPagePayload.class, SetStarredPagePayload::encode, SetStarredPagePayload::decode, "starred_item");
        registerClientbound(MenuAttachabilityPayload.class, MenuAttachabilityPayload::encode, MenuAttachabilityPayload::decode, "menu_attachability");
        registerClientbound(SyncedConfig.class, SyncedConfig::encode, SyncedConfig::decode, SyncedConfig.DEFAULT.id());
    }

    public static void initClient() {
        for (PayloadRegistration<? extends ModPacketPayload> registration : CLIENTBOUND_REGISTRATIONS) {
            registerClientReceiver(registration);
        }
    }

    // Populate encoders for client->server payloads on client side (no receiver registration)
    public static void initClientEncodersOnly() {
        SERVERBOUND.put(ItemClickPayload.class, new PayloadRegistration<>(AbstractModInitializer.withModLocation("item_click"), ItemClickPayload::encode, ItemClickPayload::decode));
        SERVERBOUND.put(CreativeItemModPayload.class, new PayloadRegistration<>(AbstractModInitializer.withModLocation("item_modify"), CreativeItemModPayload::encode, CreativeItemModPayload::decode));
        SERVERBOUND.put(ItemPageContext.class, new PayloadRegistration<>(AbstractModInitializer.withModLocation("page_context"), ItemPageContext::encode, ItemPageContext::decode));
        SERVERBOUND.put(OpenEndInvPayload.class, new PayloadRegistration<>(AbstractModInitializer.withModLocation("open_endinv"), OpenEndInvPayload::encode, OpenEndInvPayload::decode));
        SERVERBOUND.put(QuickMoveToPagePayload.class, new PayloadRegistration<>(AbstractModInitializer.withModLocation("quick_move_page"), QuickMoveToPagePayload::encode, QuickMoveToPagePayload::decode));
        SERVERBOUND.put(BulkQuickMoveFromPagePayload.class, new PayloadRegistration<>(AbstractModInitializer.withModLocation("bulk_quick_move"), BulkQuickMoveFromPagePayload::encode, BulkQuickMoveFromPagePayload::decode));
        SERVERBOUND.put(StarItemPayload.class, new PayloadRegistration<>(AbstractModInitializer.withModLocation("star_item"), StarItemPayload::encode, StarItemPayload::decode));
        SERVERBOUND.put(ToggleCraftingPayload.class, new PayloadRegistration<>(AbstractModInitializer.withModLocation("toggle_crafting"), ToggleCraftingPayload::encode, ToggleCraftingPayload::decode));
        SERVERBOUND.put(SyncedConfig.class, new PayloadRegistration<>(AbstractModInitializer.withModLocation(SyncedConfig.DEFAULT.id()), SyncedConfig::encode, SyncedConfig::decode));
        if (FabricLoader.getInstance().isModLoaded("jei")) {
            SERVERBOUND.put(com.kwwsyk.endinv.fabric.network.payloads.JeiTransferRecipePayload.class,
                    new PayloadRegistration<>(AbstractModInitializer.withModLocation("jei_transfer_recipe"),
                            com.kwwsyk.endinv.fabric.network.payloads.JeiTransferRecipePayload::encode,
                            com.kwwsyk.endinv.fabric.network.payloads.JeiTransferRecipePayload::decode));
        }
    }

    public static void sendToServer(ModPacketPayload payload) {
        PayloadRegistration<ModPacketPayload> registration = getRegistration(SERVERBOUND, payload.getClass());
        FriendlyByteBuf buf = PacketByteBufs.create();
        registration.encode(payload, buf);
        ClientPlayNetworking.send(registration.id(), buf);
    }

    public static void sendToPlayer(ServerPlayer player, ModPacketPayload payload) {
        PayloadRegistration<ModPacketPayload> registration = getRegistration(CLIENTBOUND, payload.getClass());
        FriendlyByteBuf buf = PacketByteBufs.create();
        registration.encode(payload, buf);
        ServerPlayNetworking.send(player, registration.id(), buf);
    }

    private static <T extends ModPacketPayload> void registerClientbound(Class<T> type, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, String id) {
        PayloadRegistration<T> registration = new PayloadRegistration<>(AbstractModInitializer.withModLocation(id), encoder, decoder);
        CLIENTBOUND.put(type, registration);
        CLIENTBOUND_REGISTRATIONS.add(registration);
    }

    private static <T extends ModPacketPayload> void registerServerbound(Class<T> type, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, String id) {
        PayloadRegistration<T> registration = new PayloadRegistration<>(AbstractModInitializer.withModLocation(id), encoder, decoder);
        SERVERBOUND.put(type, registration);
        ServerPlayNetworking.registerGlobalReceiver(registration.id(), (server, player, handler, buf, responseSender) -> {
            T payload = registration.decode(buf);
            server.execute(() -> payload.handle(context(player)));
        });
    }

    private static void registerClientReceiver(PayloadRegistration<? extends ModPacketPayload> registration) {
        ClientPlayNetworking.registerGlobalReceiver(registration.id(), (client, handler, buf, responseSender) -> {
            ModPacketPayload payload = registration.decode(buf);
            client.execute(() -> payload.handle(context(client.player)));
        });
    }

    private static ModPacketContext context(ServerPlayer player) {
        return () -> player;
    }

    private static ModPacketContext context(net.minecraft.world.entity.player.Player player) {
        return () -> player;
    }

    @SuppressWarnings("unchecked")
    private static PayloadRegistration<ModPacketPayload> getRegistration(Map<Class<? extends ModPacketPayload>, PayloadRegistration<? extends ModPacketPayload>> map, Class<?> type) {
        PayloadRegistration<? extends ModPacketPayload> registration = map.get(type);
        if (registration == null) {
            throw new IllegalStateException("Unregistered payload type: " + type);
        }
        return (PayloadRegistration<ModPacketPayload>) registration;
    }

    private record PayloadRegistration<T extends ModPacketPayload>(ResourceLocation id,
                                                                   BiConsumer<T, FriendlyByteBuf> encoder,
                                                                   Function<FriendlyByteBuf, T> decoder) {
        void encode(ModPacketPayload payload, FriendlyByteBuf buf) {
            encoder.accept((T) payload, buf);
        }

        T decode(FriendlyByteBuf buf) {
            return decoder.apply(buf);
        }
    }
}


