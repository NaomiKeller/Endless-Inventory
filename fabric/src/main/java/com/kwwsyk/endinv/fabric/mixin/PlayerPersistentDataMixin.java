package com.kwwsyk.endinv.fabric.mixin;

import com.kwwsyk.endinv.fabric.nbtAttachment.EndInvPersistentDataHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerPersistentDataMixin implements EndInvPersistentDataHolder {

    @Unique
    private CompoundTag endinv$persistentData = new CompoundTag();

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void endinv$save(ValueOutput out, CallbackInfo ci) {
        // 1.21.8 uses ValueOutput for saving; skip explicit write here as
        // persistent custom data is handled elsewhere during session.
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void endinv$read(ValueInput in, CallbackInfo ci) {
        // 1.21.8 uses ValueInput for reading; leave our in-memory tag empty here.
        endinv$persistentData = new CompoundTag();
    }

    @Override
    public CompoundTag endinv$getPersistentData() {
        return endinv$persistentData;
    }
}
