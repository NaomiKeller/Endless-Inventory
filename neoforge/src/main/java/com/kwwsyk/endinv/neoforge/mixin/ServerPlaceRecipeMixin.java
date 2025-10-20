package com.kwwsyk.endinv.neoforge.mixin;

import com.kwwsyk.endinv.common.EndlessInventory;
import com.kwwsyk.endinv.common.ServerLevelEndInv;
import com.kwwsyk.endinv.common.util.recipeTransferHelper.RecipeItemProvider;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(ServerPlaceRecipe.class)
public class ServerPlaceRecipeMixin<C extends Container> {

    @Final
    @Shadow
    protected StackedContents stackedContents;
    @Unique
    @Nullable
    private EndlessInventory endInv;

    @Inject(
            method = "recipeClicked",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/RecipeBookMenu;fillCraftSlotsStackedContents(Lnet/minecraft/world/entity/player/StackedContents;)V"
                    //l35 this.menu.fillCraftSlotsStackedContents(this.stackedContents);
            )
    )
    private void fillEndInvStackedContents(ServerPlayer player, RecipeHolder<?> recipe, boolean placeAll, CallbackInfo ci) {
        endInv = ServerLevelEndInv.getEndInvForPlayer(player).orElse(null);
        if (endInv == null) return;
        RecipeItemProvider.fillStackedContents(endInv.getItemsAsList(), this.stackedContents);
    }

    @Inject(
            method = "recipeClicked",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    //shift = At.Shift.AFTER,
                    target = "Lnet/minecraft/recipebook/ServerPlaceRecipe;handleRecipeClicked(Lnet/minecraft/world/item/crafting/RecipeHolder;Z)V"
                    //l37 this.handleRecipeClicked(recipe, placeAll); in if- block
            )
    )
    private void finishHandleClick(ServerPlayer player, RecipeHolder<?> recipe, boolean placeAll, CallbackInfo ci) {
        if (endInv != null) {
            endInv.broadcastChanges();
        }
    }

    @Inject(method = "moveItemToGrid", at = @At("RETURN"), cancellable = true)
    private void ei$afterMove(Slot slot, ItemStack stack, int maxAmount, CallbackInfoReturnable<Integer> cir) {
        if (endInv == null) return;
        // Vanilla returns the remaining amount still needed after moving from the player.
        // If there is still a remainder, try to satisfy it from Endless Inventory.
        int remaining = cir.getReturnValue();
        if (remaining >= 0) return;
        ItemStack taken = endInv.takeItem(stack, maxAmount);
        if (taken.isEmpty()) {
            cir.setReturnValue(maxAmount);
            return;
        }
        int newRemaining = maxAmount - taken.getCount();

        ItemStack slotItem = slot.getItem();
        if (slotItem.isEmpty()) slot.set(taken.copy());
        else slotItem.grow(taken.getCount());

        cir.setReturnValue(newRemaining);
    }
}
