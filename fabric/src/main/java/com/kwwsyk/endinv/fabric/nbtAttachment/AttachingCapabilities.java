package com.kwwsyk.endinv.fabric.nbtAttachment;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.server.level.ServerPlayer;

public class AttachingCapabilities {

    private static final String END_INV_UUID_KEY = "endinv_uuid";
    private static final String SYNCED_CONFIG_KEY = "endinv_settings";

    public static void register() {
        // no-op for Fabric; we'll attach via Player events if needed
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof ServerPlayer player) {
                var uuid = FabricNbtStorage.getUuid(player);
                if(uuid==null){
                    // assign default
                    FabricNbtStorage.setUuid(player, com.kwwsyk.endinv.common.ModInfo.DEFAULT_UUID);
                }
            }
        });
    }

}
