package com.kwwsyk.endinv.forge.integrates.jei.experimental.mixin;

import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.forge.integrates.jei.experimental.AEIRecipeTransferHandler;
import com.kwwsyk.endinv.forge.integrates.jei.experimental.AEIRecipeTransferHandler.TransferContext;
import com.kwwsyk.endinv.forge.network.payloads.JeiAttachedTransferPayload;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.common.transfer.RecipeTransferOperationsResult;
import mezz.jei.library.transfer.BasicRecipeTransferHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(targets = "mezz/jei/library/transfer/BasicRecipeTransferHandler")
public abstract class BasicRecipeTransferHandlerMixin<C extends AbstractContainerMenu, R> {

    @Shadow(remap = false)
    private IRecipeTransferInfo<C, R> transferInfo;

    private static final ThreadLocal<TransferContext> ENDINV$PLAN = new ThreadLocal<>();

    @Inject(
            method = "transferRecipe",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lmezz/jei/common/transfer/RecipeTransferUtil;getRecipeTransferOperations(Lmezz/jei/api/helpers/IStackHelper;Ljava/util/Map;Ljava/util/List;Ljava/util/List;)Lmezz/jei/common/transfer/RecipeTransferOperationsResult;",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void endinv$handleMissing(
            C container,
            R recipe,
            IRecipeSlotsView recipeSlotsView,
            Player player,
            boolean maxTransfer,
            boolean doTransfer,
            CallbackInfoReturnable<IRecipeTransferError> cir,
            List<Slot> craftingSlots,
            List<Slot> inventorySlots,
            BasicRecipeTransferHandler.InventoryState inventoryState,
            int inputCount,
            RecipeTransferOperationsResult transferOperations
    ) {
        if (transferOperations.missingItems.isEmpty()) {
            ENDINV$PLAN.remove();
            return;
        }
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
        if (context.isPresent()) {
            transferOperations.missingItems.clear();
            if (doTransfer) {
                ENDINV$PLAN.set(context.get());
            }
        } else {
            ENDINV$PLAN.remove();
        }
    }

    @Inject(
            method = "transferRecipe",
            at = @At(
                    value = "INVOKE",
                    target = "Lmezz/jei/common/network/IConnectionToServer;sendPacketToServer(Lmezz/jei/common/network/packets/PacketRecipeTransfer;)V"
            ),
            cancellable = true
    )
    private void endinv$sendAttachedTransfer(
            C container,
            R recipe,
            IRecipeSlotsView recipeSlotsView,
            Player player,
            boolean maxTransfer,
            boolean doTransfer,
            CallbackInfoReturnable<IRecipeTransferError> cir
    ) {
        TransferContext context = ENDINV$PLAN.get();
        if (context != null) {
            ENDINV$PLAN.remove();
            ModInfo.getPacketDistributor().sendToServer(new JeiAttachedTransferPayload(context));
            cir.setReturnValue(null);
            cir.cancel();
        }
    }

    @Inject(method = "transferRecipe", at = @At("RETURN"))
    private void endinv$clearPlan(CallbackInfoReturnable<IRecipeTransferError> cir) {
        ENDINV$PLAN.remove();
    }
}
