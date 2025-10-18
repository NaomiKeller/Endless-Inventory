package com.kwwsyk.endinv.fabric.mixin;

import com.kwwsyk.endinv.fabric.event.MobDeathRedirect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbAwardMixin {

    @Inject(method = "award", at = @At("HEAD"), cancellable = true)
    private static void endlessinv$redirectAward(ServerLevel level, Vec3 pos, int amount, CallbackInfo ci) {
        if (amount <= 0) return;

        ServerPlayer target = MobDeathRedirect.get();
        if (target == null) {
            var generic = level.getNearestPlayer(pos.x, pos.y, pos.z, 8.0, false);
            if (generic instanceof ServerPlayer sp) target = sp;
        }
        if (target == null) return;

        int repaired = repairPlayerItems(target, amount);
        target.giveExperiencePoints(repaired);
        ci.cancel();
    }

    private static int repairPlayerItems(ServerPlayer player, int repairAmount) {
        var entry = net.minecraft.world.item.enchantment.EnchantmentHelper.getRandomItemWith(
                net.minecraft.world.item.enchantment.Enchantments.MENDING,
                player,
                net.minecraft.world.item.ItemStack::isDamaged
        );
        if (entry != null) {
            var itemstack = entry.getValue();
            int i = Math.min(repairAmount * 2, itemstack.getDamageValue());
            itemstack.setDamageValue(itemstack.getDamageValue() - i);
            int j = repairAmount - i / 2;
            return j > 0 ? repairPlayerItems(player, j) : 0;
        } else {
            return repairAmount;
        }
    }
}
