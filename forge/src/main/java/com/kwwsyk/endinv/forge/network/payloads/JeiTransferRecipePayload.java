package com.kwwsyk.endinv.forge.network.payloads;

import com.kwwsyk.endinv.common.AbstractModInitializer;
import com.kwwsyk.endinv.common.menu.EndlessInventoryMenu;
import com.kwwsyk.endinv.common.network.payloads.ModPacketContext;
import com.kwwsyk.endinv.common.network.payloads.ModPacketPayload;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.CraftingRecipe;

import java.util.Optional;

public record JeiTransferRecipePayload(int containerId, Identifier recipeId, boolean maxTransfer) implements ModPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, JeiTransferRecipePayload> STREAM_CODEC =
            StreamCodec.of((buf, value) -> encode(value, buf), JeiTransferRecipePayload::decode);

    public static final CustomPacketPayload.Type<JeiTransferRecipePayload> TYPE =
            new CustomPacketPayload.Type<>(AbstractModInitializer.withModLocation("jei_transfer_recipe"));

    public static void encode(JeiTransferRecipePayload payload, FriendlyByteBuf buffer) {
        buffer.writeVarInt(payload.containerId);
        buffer.writeIdentifier(payload.recipeId);
        buffer.writeBoolean(payload.maxTransfer);
    }

    public static JeiTransferRecipePayload decode(FriendlyByteBuf buffer) {
        int containerId = buffer.readVarInt();
        Identifier recipeId = buffer.readIdentifier();
        boolean maxTransfer = buffer.readBoolean();
        return new JeiTransferRecipePayload(containerId, recipeId, maxTransfer);
    }

    @Override
    public String id() {
        return "jei_transfer_recipe";
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    @Override
    public void handle(ModPacketContext context) {
        Player player = context.player();
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (!(serverPlayer.containerMenu instanceof EndlessInventoryMenu menu)) {
            return;
        }
        if (menu.containerId != containerId) {
            return;
        }

        var key = ResourceKey.create(Registries.RECIPE, recipeId);
        Optional<?> optional = serverPlayer.serverLevel().getServer().getRecipeManager().byKey(key);
        optional.ifPresent(recipeObj -> {
            CraftingRecipe craftingRecipe = resolveCraftingRecipe(recipeObj);
            // JEI integration for Forge is optional/missing here; keep this a no-op to compile cleanly.
        });
    }

    private static CraftingRecipe resolveCraftingRecipe(Object recipeObj) {
        if (recipeObj instanceof CraftingRecipe craftingRecipe) {
            return craftingRecipe;
        }
        try {
            Object value = recipeObj.getClass().getMethod("value").invoke(recipeObj);
            if (value instanceof CraftingRecipe craftingRecipe) {
                return craftingRecipe;
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return null;
    }
}
