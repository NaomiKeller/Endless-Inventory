package com.kwwsyk.endinv.fabric.nbtAttachment;

import com.kwwsyk.endinv.common.network.payloads.SyncedConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ISyncedConfigImpl {

    private SyncedConfig syncedConfig = SyncedConfig.DEFAULT;
    private static final Logger LOGGER = LoggerFactory.getLogger(ISyncedConfigImpl.class);

    public SyncedConfig getSyncedConfig(){
        return syncedConfig;
    }

    public void setSyncedConfig(SyncedConfig cfg){
        this.syncedConfig = cfg;
    }

    public CompoundTag toNbt(){
        return (CompoundTag) SyncedConfig.CODEC
                .encodeStart(NbtOps.INSTANCE, this.syncedConfig)
                .getOrThrow(err -> new IllegalStateException("Failed to encode config to NBT: "+err));
    }

    public void fromNbt(CompoundTag tag){
        SyncedConfig.CODEC.parse(NbtOps.INSTANCE,tag).resultOrPartial(error-> LOGGER.error("Failed to decode config from NBT: {}",error)).ifPresent(cfg-> this.syncedConfig = cfg);
    }
}
