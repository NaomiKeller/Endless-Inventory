package com.kwwsyk.endinv.forge.integrates.jei.experimental.mixin;

import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.forge.integrates.jei.experimental.AEIRecipeTransferHandler;
import com.kwwsyk.endinv.forge.network.payloads.JeiAttachedTransferPayload;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.library.transfer.BasicRecipeTransferHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = BasicRecipeTransferHandler.class, remap = false)
public abstract class BasicRecipeTransferHandlerMixin<C extends AbstractContainerMenu, R> {

    @Final
    @Shadow(remap = false)
    private IRecipeTransferInfo<C, R> transferInfo;

    // Removed ThreadLocal plan; sending handled directly in first injection

    @Inject(method = "transferRecipe", at = @At("HEAD"), cancellable = true, remap = false)
    private void endinv$handleMissing(
            C container,
            R recipe,
            IRecipeSlotsView recipeSlotsView,
            Player player,
            boolean maxTransfer,
            boolean doTransfer,
            CallbackInfoReturnable<IRecipeTransferError> cir
    ) {
        List<Slot> craftingSlots = transferInfo.getRecipeSlots(container, recipe);
        List<Slot> inventorySlots = transferInfo.getInventorySlots(container, recipe);
        var context = AEIRecipeTransferHandler.prepareClientContext(
                container,
                recipe,
                recipeSlotsView,
                player,
                maxTransfer,
                transferInfo,
                craftingSlots,
                inventorySlots
        );
        if (context.isPresent() && doTransfer) {
            ModInfo.getPacketDistributor().sendToServer(new JeiAttachedTransferPayload(context.get()));
            cir.setReturnValue(null);
            cir.cancel();
        }
    }

    // The previous injection that hooked the network send was removed
    // because its target could not be resolved in current JEI.
}
