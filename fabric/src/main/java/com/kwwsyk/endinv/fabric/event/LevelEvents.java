package com.kwwsyk.endinv.fabric.event;

import com.kwwsyk.endinv.common.data.EndlessInventoryData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

public final class LevelEvents {

    private LevelEvents() {
    }

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(LevelEvents::onServerStarted);
    }

    private static void onServerStarted(MinecraftServer server) {
        for (var level : server.getAllLevels()) {
            EndlessInventoryData.init(level);
        }
        PlayerEvents.markPlayersForSync(server);
    }
}
