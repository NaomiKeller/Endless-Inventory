package com.kwwsyk.endinv.forge.mixin;

import com.kwwsyk.endinv.common.EndlessInventory;
import com.kwwsyk.endinv.common.ServerLevelEndInv;
import com.kwwsyk.endinv.common.util.recipeTransferHelper.RecipeItemProvider;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.annotation.Nullable;

@Mixin(ServerPlaceRecipe.class)
public class ServerPlaceRecipeMixin<C extends Container>{

    @Final
    @Shadow
    protected StackedContents stackedContents;
    @Unique
    @Nullable
    private EndlessInventory endInv;


    @Inject(method = "recipeClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/RecipeBookMenu;fillCraftSlotsStackedContents(Lnet/minecraft/world/entity/player/StackedContents;)V"))
    private void fillEndInvStackedContents(ServerPlayer player, Recipe<C> recipe, boolean placeAll, CallbackInfo ci){
        endInv = ServerLevelEndInv.getEndInvForPlayer(player).orElse(null);
        if(endInv==null) return;
        RecipeItemProvider.fillStackedContents(endInv.getItemsAsList(), this.stackedContents);
    }

    @Inject(method = "recipeClicked", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/recipebook/ServerPlaceRecipe;handleRecipeClicked(Lnet/minecraft/world/item/crafting/Recipe;Z)V"))
    private void finishHandleClick(ServerPlayer player, Recipe<C> recipe, boolean placeAll, CallbackInfo ci){
        if(endInv!=null){
            endInv.broadcastChanges();
        }
    }

    @Inject(method = "moveItemToGrid",
            at = @At("RETURN"),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void getAttachedItems(Slot slotToFill, ItemStack ingredient, CallbackInfo ci, int i, ItemStack itemstack){
        if(i!=-1 || endInv==null) return;
        ItemStack itemStack1 = endInv.takeItem(ingredient,1);
        //endInv.broadcastChanges(); Don't let it be invoked too many times.
        if(itemStack1.isEmpty()){
            return;
        }
        if(slotToFill.getItem().isEmpty()){
            slotToFill.set(itemStack1.copy());
        }else {
            slotToFill.getItem().grow(itemStack1.getCount());
        }
    }
}
