package com.kwwsyk.endinv.fabric.nbtAttachment;

import com.kwwsyk.endinv.common.AbstractModInitializer;
import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.common.network.payloads.SyncedConfig;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.UUIDUtil;

import java.util.UUID;
@SuppressWarnings("UnstableApiUsage")
public final class FabricAttachment {

    public static final AttachmentType<UUID> ENDINV_UUID = AttachmentRegistry.create(
            AbstractModInitializer.withModLocation("endinv_uuid"),
            builder -> builder
                    .initializer(()-> ModInfo.DEFAULT_UUID)
                    .persistent(UUIDUtil.CODEC)
                    .copyOnDeath()
                    .syncWith(
                            UUIDUtil.STREAM_CODEC,
                            AttachmentSyncPredicate.targetOnly()
                    )
    );

    public static final AttachmentType<SyncedConfig> SYNCED_CONFIG = AttachmentRegistry.create(
            AbstractModInitializer.withModLocation("synced_config"),
            builder -> builder
                    .initializer(()-> SyncedConfig.DEFAULT)
                    .persistent(SyncedConfig.CODEC)
                    .copyOnDeath()
                    .syncWith(
                            SyncedConfig.STREAM_CODEC,
                            AttachmentSyncPredicate.targetOnly()
                    )
    );


}
