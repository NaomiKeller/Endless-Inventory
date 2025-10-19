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
import org.spongepowered.asm.mixin.injection.ModifyVariable;
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
    @Unique
    private int ei$lastIndex = Integer.MIN_VALUE;

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

    @ModifyVariable(method = "moveItemToGrid", at = @At(value = "STORE"), ordinal = 0, argsOnly = true)
    private int ei$captureIndex(int i) { this.ei$lastIndex = i; return i; }

    @Inject(method = "moveItemToGrid", at = @At("RETURN"), cancellable = true)
    private void ei$afterMove(Slot slot, ItemStack stack, int maxAmount, CallbackInfoReturnable<Integer> cir) {
        if (this.ei$lastIndex != -1) return;
        if (endInv == null) return;
        ItemStack taken = endInv.takeItem(stack, maxAmount);
        if (taken.isEmpty()) return;
        int count = taken.getCount();//count<=maxAmount
        int j = maxAmount - count;

        ItemStack cur = slot.getItem();//line 150-154
        if (cur.isEmpty()) slot.set(taken.copy());
        else cur.grow(taken.getCount());

        cir.setReturnValue(j);
    }
}
