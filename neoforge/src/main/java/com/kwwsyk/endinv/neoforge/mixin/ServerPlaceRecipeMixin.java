package com.kwwsyk.endinv.neoforge.mixin;

import com.kwwsyk.endinv.common.EndlessInventory;
import com.kwwsyk.endinv.common.ServerLevelEndInv;
import com.kwwsyk.endinv.common.util.recipeTransferHelper.RecipeItemProvider;
import net.minecraft.core.Holder;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(ServerPlaceRecipe.class)
public class ServerPlaceRecipeMixin {


    @Unique
    @Nullable
    private static EndlessInventory endInv;

    @Inject(
            method = "placeRecipe(Lnet/minecraft/recipebook/ServerPlaceRecipe$CraftingMenuAccess;IILjava/util/List;Ljava/util/List;Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/item/crafting/RecipeHolder;ZZ)Lnet/minecraft/world/inventory/RecipeBookMenu$PostPlaceAction;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/recipebook/ServerPlaceRecipe$CraftingMenuAccess;fillCraftSlotsStackedContents(Lnet/minecraft/world/entity/player/StackedItemContents;)V"
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private static <R extends Recipe<?>> void fillEndInvStackedContents(
            ServerPlaceRecipe.CraftingMenuAccess<R> menu,
            int gridWidth, int gridHeight,
            List<Slot> inputGridSlots, List<Slot> slotsToClear,
            Inventory inventory, RecipeHolder<R> recipe,
            boolean useMaxItems, boolean isCreative,
            CallbackInfoReturnable<RecipeBookMenu.PostPlaceAction> cir,
            ServerPlaceRecipe serverplacerecipe,
            StackedItemContents stackeditemcontents
    ) {
        endInv = ServerLevelEndInv.getEndInvForPlayer(inventory.player).orElse(null);
        if (endInv == null) return;
        RecipeItemProvider.fillStackedItemContents(endInv.getItemsAsList(), stackeditemcontents);
    }

    @Inject(
            method = "placeRecipe(Lnet/minecraft/recipebook/ServerPlaceRecipe$CraftingMenuAccess;IILjava/util/List;Ljava/util/List;Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/item/crafting/RecipeHolder;ZZ)Lnet/minecraft/world/inventory/RecipeBookMenu$PostPlaceAction;",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/recipebook/ServerPlaceRecipe$CraftingMenuAccess;fillCraftSlotsStackedContents(Lnet/minecraft/world/entity/player/StackedItemContents;)V"
            )
    )
    private static <R extends Recipe<?>> void finishHandleClick(
            ServerPlaceRecipe.CraftingMenuAccess<R> menu,
            int gridWidth, int gridHeight,
            List<Slot> inputGridSlots, List<Slot> slotsToClear,
            Inventory inventory, RecipeHolder<R> recipe,
            boolean useMaxItems, boolean isCreative,
            CallbackInfoReturnable<RecipeBookMenu.PostPlaceAction> cir
    ) {
        if (endInv != null) {
            endInv.broadcastChanges();
        }
    }

    @Inject(method = "moveItemToGrid", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void ei$afterMove(Slot slot, Holder<Item> item, int count, CallbackInfoReturnable<Integer> cir, ItemStack slotStack) {
        if (endInv == null) return;
        // Vanilla returns the remaining amount still needed after moving from the player.
        // If there is still a remainder, try to satisfy it from Endless Inventory.
        int remaining = cir.getReturnValue();
        assert remaining == -1;
        if (remaining >= 0) return;
        //should simulate Inventory#findSlotMatchingCraftingIngredient
        ItemStack key = item.value().getDefaultInstance();
        int slotStackCount;
        if(slotStack.isEmpty() || ItemStack.isSameItemSameComponents(slotStack, key)){
            slotStackCount = slotStack.getCount();
        }else if(slotStack.is(item)) {
            key = slotStack.copyWithCount(1);
            slotStackCount = slotStack.getCount();
        }else return;
        ItemStack taken = endInv.takeItem(key, count - slotStackCount);
        if (taken.isEmpty()) {
            return;
        }

        int newRemaining = count - taken.getCount();

        assert ItemStack.isSameItemSameComponents(taken, slotStack);
        if (slotStack.isEmpty()) slot.set(taken.copy());
        else slotStack.grow(taken.getCount());

        cir.setReturnValue(newRemaining);
    }
}
