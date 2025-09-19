package com.kwwsyk.endinv.fabric.event;

import com.kwwsyk.endinv.common.data.EndlessInventoryData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

public final class LevelEvents {

    private LevelEvents() {
    }

    public static void register() {
        ServerWorldEvents.LOAD.register(LevelEvents::onLoad);
    }

    private static void onLoad(MinecraftServer server, ServerLevel level) {
        EndlessInventoryData.init(level);
        PlayerEvents.markPlayersForSync(server);
    }
}
