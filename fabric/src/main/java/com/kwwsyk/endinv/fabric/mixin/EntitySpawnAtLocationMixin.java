package com.kwwsyk.endinv.fabric.mixin;

import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.common.ServerLevelEndInv;
import com.kwwsyk.endinv.common.network.payloads.toClient.ItemPickedUpPayload;
import com.kwwsyk.endinv.fabric.event.MobDeathRedirect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntitySpawnAtLocationMixin {

    @Inject(method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At("HEAD"), cancellable = true)
    private void endlessinv$absorbDeathDrops(ItemStack stack, CallbackInfoReturnable<ItemEntity> cir) {
        ServerPlayer killer = MobDeathRedirect.get();
        if (killer == null) return;
        Entity self = (Entity) (Object) this;
        if (!(self instanceof LivingEntity)) return;
        if (stack == null || stack.isEmpty()) return;

        ServerLevelEndInv.getEndInvForPlayer(killer).ifPresent(endInv -> {
            ItemStack remain = endInv.addItem(stack.copy());
            int inserted = stack.getCount() - remain.getCount();
            if (inserted > 0) {
                ItemStack picked = stack.copy();
                picked.setCount(inserted);
                ModInfo.getPacketDistributor().sendToPlayer(killer, new ItemPickedUpPayload(picked));
            }

            if (remain.isEmpty()) {
                cir.setReturnValue(null); // no entity spawned
                cir.cancel();
            } else {
                // spawn only remaining part by delegating to vanilla later; replace argument
                stack.setCount(remain.getCount());
            }
        });
    }
}

