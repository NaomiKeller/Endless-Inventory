package com.kwwsyk.endinv.fabric.nbtAttachment;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public final class FabricNbtStorage {

    private static final String ROOT_KEY = "endinv_data";
    private static final String UUID_KEY = "endinv_uuid";

    private FabricNbtStorage() {
    }

    public static UUID getUuid(Player player) {
        CompoundTag root = root(player);
        if (!root.contains(UUID_KEY)) return null;
        return root.getString(UUID_KEY).map(s -> {
            try {
                return s != null && !s.isEmpty() ? UUID.fromString(s) : null;
            } catch (Throwable t) {
                return null;
            }
        }).orElse(null);
    }

    public static void setUuid(Player player, UUID uuid) {
        root(player).putString(UUID_KEY, uuid.toString());
    }

    public static CompoundTag getCompound(Player player, String key) {
        CompoundTag root = root(player);
        return root.getCompound(key).orElse(new CompoundTag());
    }

    public static void setCompound(Player player, String key, CompoundTag value) {
        System.out.println("[EndlessInventory] setCompound(" + key + ") = " + value);
        root(player).put(key, value);
    }

    public static boolean hasCompound(Player player, String key) {
        return root(player).contains(key);
    }

    private static CompoundTag root(Player player) {
        CompoundTag persistent = ((EndInvPersistentDataHolder) player).endinv$getPersistentData();
        var opt = persistent.getCompound(ROOT_KEY);
        if (opt.isPresent()) return opt.get();
        CompoundTag created = new CompoundTag();
        persistent.put(ROOT_KEY, created);
        return created;
    }
}



