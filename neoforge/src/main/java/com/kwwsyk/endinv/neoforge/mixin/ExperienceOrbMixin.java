package com.kwwsyk.endinv.neoforge.mixin;


import com.kwwsyk.endinv.common.options.ServerConfigs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbMixin {

    @Shadow private int value;

    @Unique private boolean suit$protectApplied = false;

    @Inject(method = "tick", at = @At("TAIL"))
    private void suit$adjustXpOrbBehavior(CallbackInfo ci) {
        Entity self = (Entity)(Object)this;

        // One-time protection when enabled
        if (ServerConfigs.PICKUP_HELPER.EXP_DROPS.PROTECT_DROPS.get() && !suit$protectApplied) {
            self.invulnerableTime = 6000;
            suit$protectApplied = true;
        }

        Player nearest = self.level().getNearestPlayer(self, 16.0);
        if (nearest == null) return;

        if(ServerConfigs.PICKUP_HELPER.EXP_DROPS.TOUCH_DIRECTLY.get()){
            if (!self.level().isClientSide) {
                self.playerTouch(nearest);
                nearest.takeXpDelay = 0;
            }
            return;
        }

        // Direct delivery when enabled (covers block XP or any orb)
        if (ServerConfigs.PICKUP_HELPER.EXP_DROPS.GIVE_DIRECTLY.get()) {
            if (!self.level().isClientSide) {
                nearest.giveExperiencePoints(this.value);
                self.discard();
            }
            return;
        }

        int v = ServerConfigs.PICKUP_HELPER.EXP_DROPS.DIRECTED_DISTRIBUTE.get();
        if (v != 0) {
            if (v < 0) {
                // Teleport to player
                Vec3 to = nearest.position();
                self.setPos(to);
            } else {
                // Steer toward player's upper body
                Vec3 from = self.position();
                Vec3 to = nearest.position().add(0.0, nearest.getBbHeight() * 0.75, 0.0);
                Vec3 dir = to.subtract(from);
                if (dir.lengthSqr() > 1.0e-6) {
                    dir = dir.normalize().scale(0.05 * v);
                    self.setDeltaMovement(dir);
                }
            }
        }
    }
}

