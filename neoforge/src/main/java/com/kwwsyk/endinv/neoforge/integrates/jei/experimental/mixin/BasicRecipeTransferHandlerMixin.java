package com.kwwsyk.endinv.neoforge.integrates.jei.experimental.mixin;

import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.common.options.ContentTransferMode;
import com.kwwsyk.endinv.neoforge.integrates.jei.experimental.AEIRecipeTransferHandler;
import com.kwwsyk.endinv.neoforge.integrates.jei.experimental.AEIRecipeTransferHandler.TransferContext;
import com.kwwsyk.endinv.neoforge.network.payloads.JeiAttachedTransferPayload;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.transfer.RecipeTransferOperationsResult;
import mezz.jei.library.transfer.BasicRecipeTransferHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(value = BasicRecipeTransferHandler.class)
public abstract class BasicRecipeTransferHandlerMixin<C extends AbstractContainerMenu, R> {

    @Final
    @Shadow(remap = false)
    private IRecipeTransferInfo<C, R> transferInfo;

    @Unique
    private static final ThreadLocal<TransferContext> ENDINV$PLAN = new ThreadLocal<>();
    @Unique
    private static final ThreadLocal<Boolean> ENDINV$ALLOW_BUTTON = new ThreadLocal<>();

    @Inject(
            method = "transferRecipe",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lmezz/jei/common/transfer/RecipeTransferUtil;getRecipeTransferOperations(Lmezz/jei/api/helpers/IStackHelper;Ljava/util/Map;Ljava/util/List;Ljava/util/List;)Lmezz/jei/common/transfer/RecipeTransferOperationsResult;",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            remap = false
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
            List<IRecipeSlotsView> inputItemSlotViews,
            BasicRecipeTransferHandler.InventoryState inventoryState,
            int inputCount,
            RecipeTransferOperationsResult transferOperations
    ) {
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
            } else {
                ENDINV$ALLOW_BUTTON.set(Boolean.TRUE);
            }
        } else {
            ENDINV$PLAN.remove();
            ENDINV$ALLOW_BUTTON.remove();
        }
    }

    @Redirect(
            method = "transferRecipe",
            at = @At(
                    value = "INVOKE",
                    target = "Lmezz/jei/common/network/IConnectionToServer;sendPacketToServer(Lmezz/jei/common/network/packets/PlayToServerPacket;)V"
            ),
            remap = false
    )
    private <T extends mezz.jei.common.network.packets.PlayToServerPacket<T>> void endinv$redirectTransferPacket(IConnectionToServer connection, T packet) {
        TransferContext context = ENDINV$PLAN.get();
        if (context != null) {
            ENDINV$PLAN.remove();
            ModInfo.getPacketDistributor().sendToServer(new JeiAttachedTransferPayload(context));
        } else {
            // Fallback: find a compatible sendPacketToServer method by name and single parameter
            // JEI uses an interface type for the packet; avoid exact-class lookup.
            try {
                boolean invoked = false;
                Class<?> cls = connection.getClass();
                while (cls != null && !invoked) {
                    for (var m : cls.getMethods()) {
                        if (!m.getName().equals("sendPacketToServer")) continue;
                        var params = m.getParameterTypes();
                        if (params.length != 1) continue;
                        if (params[0].isInstance(packet)) {
                            m.invoke(connection, packet);
                            invoked = true;
                            break;
                        }
                    }
                    cls = cls.getSuperclass();
                }
                if (!invoked) {
                    // Last resort: try declared methods as well
                    for (var m : connection.getClass().getDeclaredMethods()) {
                        if (!m.getName().equals("sendPacketToServer")) continue;
                        var params = m.getParameterTypes();
                        if (params.length != 1) continue;
                        if (params[0].isInstance(packet)) {
                            m.setAccessible(true);
                            m.invoke(connection, packet);
                            break;
                        }
                    }
                }
            } catch (Throwable ignored) {}
        }
    }

    @Inject(method = "transferRecipe", at = @At("RETURN"), remap = false, cancellable = true)
    private void endinv$clearPlan(CallbackInfoReturnable<IRecipeTransferError> cir) {
        try {
            Boolean allow = ENDINV$ALLOW_BUTTON.get();
            if (allow != null && allow && cir.getReturnValue() != null) {
                cir.setReturnValue(null);
            }
            if (com.kwwsyk.endinv.common.options.ServerConfigs.ENDINV_BEHAVIOR.TransferMode.get() == ContentTransferMode.ALL) {
                IRecipeTransferError err = cir.getReturnValue();
                if (err != null) {
                    cir.setReturnValue(() -> IRecipeTransferError.Type.COSMETIC);
                }
            }
        } finally {
            ENDINV$PLAN.remove();
            ENDINV$ALLOW_BUTTON.remove();
        }
    }
}
