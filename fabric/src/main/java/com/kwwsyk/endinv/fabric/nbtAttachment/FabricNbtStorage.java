package com.kwwsyk.endinv.fabric.nbtAttachment;

import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public final class FabricNbtStorage {

    private static final String UUID_KEY = "endinv_uuid";

    private FabricNbtStorage() {
    }

    public static UUID getUuid(Player player) {
        if (player.getPersistentData().hasUUID(UUID_KEY)) {
            return player.getPersistentData().getUUID(UUID_KEY);
        }
        return null;
    }

    public static void setUuid(Player player, UUID uuid) {
        player.getPersistentData().putUUID(UUID_KEY, uuid);
    }
}
