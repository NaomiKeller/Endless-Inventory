package com.kwwsyk.endinv.forge.network.payloads;

import com.kwwsyk.endinv.common.AbstractModInitializer;
import com.kwwsyk.endinv.common.menu.EndlessInventoryMenu;
import com.kwwsyk.endinv.common.network.payloads.ModPacketContext;
import com.kwwsyk.endinv.common.network.payloads.ModPacketPayload;
import com.kwwsyk.endinv.forge.integrates.jei.EIMRecipeTranHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraftforge.fml.ModList;

import java.util.Optional;

public record JeiTransferRecipePayload(int containerId, ResourceLocation recipeId, boolean maxTransfer) implements ModPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, JeiTransferRecipePayload> STREAM_CODEC =
            StreamCodec.of((buf, value) -> encode(value, buf), JeiTransferRecipePayload::decode);

    public static final CustomPacketPayload.Type<JeiTransferRecipePayload> TYPE =
            new CustomPacketPayload.Type<>(AbstractModInitializer.withModLocation("jei_transfer_recipe"));

    public static void encode(JeiTransferRecipePayload payload, FriendlyByteBuf buffer) {
        buffer.writeVarInt(payload.containerId);
        buffer.writeResourceLocation(payload.recipeId);
        buffer.writeBoolean(payload.maxTransfer);
    }

    public static JeiTransferRecipePayload decode(FriendlyByteBuf buffer) {
        int containerId = buffer.readVarInt();
        ResourceLocation recipeId = buffer.readResourceLocation();
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
        if (!ModList.get().isLoaded("jei")) {
            return;
        }
        if (!(serverPlayer.containerMenu instanceof EndlessInventoryMenu menu)) {
            return;
        }
        if (menu.containerId != containerId) {
            return;
        }

        Optional<?> optional = serverPlayer.serverLevel().getRecipeManager().byKey(recipeId);
        optional.ifPresent(recipeObj -> {
            CraftingRecipe craftingRecipe = resolveCraftingRecipe(recipeObj);
            if (craftingRecipe != null) {
                EIMRecipeTranHandler.performServerTransfer(menu, craftingRecipe, serverPlayer, maxTransfer);
            }
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
