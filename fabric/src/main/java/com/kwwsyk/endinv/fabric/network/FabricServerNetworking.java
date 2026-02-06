package com.kwwsyk.endinv.fabric.network;

import com.kwwsyk.endinv.common.AbstractModInitializer;
import com.kwwsyk.endinv.common.network.payloads.ModPacketContext;
import com.kwwsyk.endinv.common.network.payloads.ModPacketPayload;
import com.kwwsyk.endinv.common.network.payloads.SyncedConfig;
import com.kwwsyk.endinv.common.network.payloads.toServer.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;
import java.util.function.Function;

public final class FabricServerNetworking {

    private FabricServerNetworking() {}

    public static void init() {
        register(ItemClickPayload.class, ItemClickPayload::encode, ItemClickPayload::decode, "item_click");
        register(CreativeItemModPayload.class, CreativeItemModPayload::encode, CreativeItemModPayload::decode, "item_modify");
        register(ItemPageContext.class, ItemPageContext::encode, ItemPageContext::decode, "page_context");
        register(OpenEndInvPayload.class, OpenEndInvPayload::encode, OpenEndInvPayload::decode, "open_endinv");
        register(QuickMoveToPagePayload.class, QuickMoveToPagePayload::encode, QuickMoveToPagePayload::decode, "quick_move_page");
        register(StarItemPayload.class, StarItemPayload::encode, StarItemPayload::decode, "star_item");
        register(ToggleCraftingPayload.class, ToggleCraftingPayload::encode, ToggleCraftingPayload::decode, "toggle_crafting");
        register(SyncedConfig.class, SyncedConfig::encode, SyncedConfig::decode, SyncedConfig.DEFAULT.id());
        register(BulkQuickMoveFromPagePayload.class, BulkQuickMoveFromPagePayload::encode, BulkQuickMoveFromPagePayload::decode, "bulk_quick_move");
        if (FabricLoader.getInstance().isModLoaded("jei")) {
            register(com.kwwsyk.endinv.fabric.network.payloads.JeiTransferRecipePayload.class,
                    com.kwwsyk.endinv.fabric.network.payloads.JeiTransferRecipePayload::encode,
                    com.kwwsyk.endinv.fabric.network.payloads.JeiTransferRecipePayload::decode,
                    "jei_transfer_recipe");
        }
    }

    private static <T extends ModPacketPayload> void register(Class<T> type,
                                                             BiConsumer<T, FriendlyByteBuf> encoder,
                                                             Function<FriendlyByteBuf, T> decoder,
                                                             String id) {
        ResourceLocation rid = AbstractModInitializer.withModLocation(id);
        ServerPlayNetworking.registerGlobalReceiver(rid, (server, player, handler, buf, response) -> {
            T payload = decoder.apply(buf);
            server.execute(() -> payload.handle(context(player)));
        });
    }

    private static ModPacketContext context(ServerPlayer player) {
        return () -> player;
    }
}

