package com.kwwsyk.endinv.neoforge.mixin;

import com.kwwsyk.endinv.common.options.ServerConfigs;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import static com.kwwsyk.endinv.common.autopick.AutoPickHelper.isPlayerEnabledAutoPick;
import static com.kwwsyk.endinv.neoforge.events.LootEvent.CapturedDrops;
import static com.kwwsyk.endinv.neoforge.events.LootEvent.sendItemToEndinv;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {

    @Shadow
    private int pickupDelay;

    @Unique
    private ItemEntity self =  (ItemEntity)(Object)this;

    @WrapOperation(
            method = "playerTouch",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z"
            )
    )
    private boolean endinv$pickupIntoEndlessInventory(
            Inventory inventory,
            ItemStack itemStack,
            Operation<Boolean> original,
            Player player
    ){
        if(
                player instanceof ServerPlayer && isPlayerEnabledAutoPick(player)
                && (ServerConfigs.PICKUP_HELPER.ITEM_DROPS.ENDINV_AFTER_INVENTORY.get() && CapturedDrops.contains(self)
                        || ServerConfigs.PICKUP_HELPER.ITEM_DROPS.PICK_TO_ENDINV.get())
        ){
            ItemStack remain = sendItemToEndinv(player, itemStack);
            itemStack.setCount(remain.getCount());
            if(remain.isEmpty()) return true;
            return original.call(inventory, itemStack);
        }
        return original.call(inventory, itemStack);
    }
}
