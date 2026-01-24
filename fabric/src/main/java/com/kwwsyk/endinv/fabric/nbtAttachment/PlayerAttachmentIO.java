package com.kwwsyk.endinv.fabric.nbtAttachment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kwwsyk.endinv.common.ModRegistries;
import com.kwwsyk.endinv.common.network.payloads.SyncedConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.LevelResource;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Simple world-scoped persistence for player attachments on Fabric.
 * Avoids relying on entity additional save hooks (ValueInput/Output) which vary per mapping.
 */
public final class PlayerAttachmentIO {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String KEY_UUID = "endinv_uuid";
    private static final String KEY_SETTINGS = "endinv_settings";

    private PlayerAttachmentIO() {}

    public static void loadFor(ServerPlayer player) {
        Path path = ((net.minecraft.server.level.ServerLevel) player.level()).getServer().getWorldPath(LevelResource.ROOT)
                .resolve("data").resolve("endinv_playerdata")
                .resolve(player.getUUID().toString() + ".json");
        if (!Files.exists(path)) return;
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonObject root = GsonHelper.parse(reader);
            if (root.has(KEY_UUID)) {
                String uuidStr = GsonHelper.getAsString(root, KEY_UUID, null);
                if (uuidStr != null && !uuidStr.isEmpty()) {
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        ModRegistries.NbtAttachments.getEndInvUUID().setTo(player, uuid);
                    } catch (IllegalArgumentException ignored) {}
                }
            }
            if (root.has(KEY_SETTINGS) && root.get(KEY_SETTINGS).isJsonObject()) {
                JsonElement elem = root.get(KEY_SETTINGS);
                SyncedConfig cfg = SyncedConfig.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, elem)
                        .result().orElse(SyncedConfig.DEFAULT);
                ModRegistries.NbtAttachments.getSyncedConfig().setTo(player, cfg);
            }
        } catch (IOException ignored) {}
    }

    public static void saveFor(ServerPlayer player) {
        Path dir = ((net.minecraft.server.level.ServerLevel) player.level()).getServer().getWorldPath(LevelResource.ROOT)
                .resolve("data").resolve("endinv_playerdata");
        Path path = dir.resolve(player.getUUID().toString() + ".json");
        try {
            Files.createDirectories(dir);
            JsonObject root = new JsonObject();
            UUID uuid = ModRegistries.NbtAttachments.getEndInvUUID().getWith(player);
            root.addProperty(KEY_UUID, uuid != null ? uuid.toString() : "");
            SyncedConfig cfg = ModRegistries.NbtAttachments.getSyncedConfig().getWith(player);
            JsonElement elem = SyncedConfig.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, cfg).result().orElseGet(JsonObject::new);
            root.add(KEY_SETTINGS, elem);
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                GSON.toJson(root, writer);
            }
        } catch (IOException ignored) {}
    }
}
