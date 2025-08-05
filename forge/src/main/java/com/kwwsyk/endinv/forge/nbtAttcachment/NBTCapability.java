package com.kwwsyk.endinv.forge.nbtAttcachment;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NBTCapability<N extends INBTSerializable<CompoundTag>> implements ICapabilitySerializable<CompoundTag> {

    N backend;
    Capability<N> capability;
    LazyOptional<? super N> optional = LazyOptional.of(()->backend);

    public NBTCapability(@NotNull N attachment, Capability<N> capability){
        this.backend = attachment;
        this.capability = capability;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction direction) {
        return capability==this.capability ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return backend.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag compoundTag) {
        backend.deserializeNBT(compoundTag);
    }
}
