package com.kwwsyk.endinv.forge.network.payloads;

import com.kwwsyk.endinv.common.network.payloads.ModPacketContext;
import com.kwwsyk.endinv.common.network.payloads.ModPacketPayload;
import com.kwwsyk.endinv.forge.integrates.jei.experimental.AEIRecipeTransferHandler;
import com.kwwsyk.endinv.forge.integrates.jei.experimental.AEIRecipeTransferHandler.TransferContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraftforge.fml.ModList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record JeiAttachedTransferPayload(TransferContext context) implements ModPacketPayload {

    public static void encode(JeiAttachedTransferPayload payload, FriendlyByteBuf buffer) {
        buffer.writeVarInt(payload.context.containerId());
        buffer.writeResourceLocation(payload.context.recipeId());
        buffer.writeBoolean(payload.context.maxTransfer());
        buffer.writeBoolean(payload.context.requireCompleteSets());
        buffer.writeVarInt(payload.context.craftingSlotIndexes().size());
        payload.context.craftingSlotIndexes().forEach(buffer::writeVarInt);
        buffer.writeVarInt(payload.context.inventorySlotIndexes().size());
        payload.context.inventorySlotIndexes().forEach(buffer::writeVarInt);
    }

    public static JeiAttachedTransferPayload decode(FriendlyByteBuf buffer) {
        int containerId = buffer.readVarInt();
        ResourceLocation recipeId = buffer.readResourceLocation();
        boolean maxTransfer = buffer.readBoolean();
        boolean requireCompleteSets = buffer.readBoolean();
        int craftingSize = buffer.readVarInt();
        List<Integer> craftingIndexes = new ArrayList<>(craftingSize);
        for (int i = 0; i < craftingSize; i++) {
            craftingIndexes.add(buffer.readVarInt());
        }
        int inventorySize = buffer.readVarInt();
        List<Integer> inventoryIndexes = new ArrayList<>(inventorySize);
        for (int i = 0; i < inventorySize; i++) {
            inventoryIndexes.add(buffer.readVarInt());
        }
        TransferContext context = new TransferContext(
                containerId,
                recipeId,
                maxTransfer,
                requireCompleteSets,
                craftingIndexes,
                inventoryIndexes
        );
        return new JeiAttachedTransferPayload(context);
    }

    @Override
    public String id() {
        return "jei_attached_transfer";
    }

    @Override
    public void handle(ModPacketContext modContext) {
        if (!(modContext.player() instanceof ServerPlayer player)) {
            return;
        }
        if (!ModList.get().isLoaded("jei")) {
            return;
        }
        AbstractContainerMenu container = player.containerMenu;
        if (container == null || container.containerId != context.containerId()) {
            return;
        }
        Optional<?> optional = player.serverLevel().getRecipeManager().byKey(context.recipeId());
        if (optional.isEmpty()) {
            return;
        }
        CraftingRecipe craftingRecipe = resolve(optional.get());
        if (craftingRecipe == null) {
            return;
        }
        var manager = AEIRecipeTransferHandler.getServerManager(player);
        if (manager == null || manager.getMenu() != container) {
            return;
        }
        List<Slot> craftingSlots = collectSlots(container, context.craftingSlotIndexes());
        List<Slot> inventorySlots = collectSlots(container, context.inventorySlotIndexes());
        if (craftingSlots.size() != context.craftingSlotIndexes().size() ||
                inventorySlots.size() != context.inventorySlotIndexes().size()) {
            return;
        }
        AEIRecipeTransferHandler.performServerTransfer(
                container,
                craftingRecipe,
                craftingSlots,
                inventorySlots,
                player,
                manager,
                context.maxTransfer(),
                context.requireCompleteSets()
        );
    }

    private static CraftingRecipe resolve(Object recipeObj) {
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

    private static List<Slot> collectSlots(AbstractContainerMenu container, List<Integer> indexes) {
        List<Slot> slots = new ArrayList<>(indexes.size());
        for (int index : indexes) {
            if (index >= 0 && index < container.slots.size()) {
                slots.add(container.getSlot(index));
            }
        }
        return slots;
    }
}
