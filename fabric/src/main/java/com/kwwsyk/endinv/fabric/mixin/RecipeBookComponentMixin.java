package com.kwwsyk.endinv.fabric.mixin;

import com.kwwsyk.endinv.common.client.CachedSrcInv;
import com.kwwsyk.endinv.common.util.recipeTransferHelper.RecipeItemProvider;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.world.entity.player.StackedContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(RecipeBookComponent.class)
public class RecipeBookComponentMixin {

    @Unique
    private CachedSrcInv srcInv = CachedSrcInv.INSTANCE;

    @ModifyArg(method = "initVisuals", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/RecipeBookMenu;fillCraftSlotsStackedContents(Lnet/minecraft/world/entity/player/StackedContents;)V"), index = 0)
    private StackedContents endinv$fillEndInvStackedContents(StackedContents original) {
        RecipeItemProvider.fillStackedContents(srcInv.getItemsAsList(), original);
        return original;
    }

    @ModifyArg(method = "updateStackedContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/RecipeBookMenu;fillCraftSlotsStackedContents(Lnet/minecraft/world/entity/player/StackedContents;)V"), index = 0)
    private StackedContents endinv$updateStackedContentsOfEndInv(StackedContents original){
        RecipeItemProvider.fillStackedContents(srcInv.getItemsAsList(), original);
        return original;
    }
}
