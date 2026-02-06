package com.kwwsyk.endinv.fabric.event;

import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.common.ModRegistries;
import com.kwwsyk.endinv.common.ServerLevelEndInv;
import com.kwwsyk.endinv.common.network.payloads.SyncedConfig;
import com.kwwsyk.endinv.common.network.payloads.toClient.EndInvContent;
import com.kwwsyk.endinv.common.network.payloads.toClient.EndInvMetadata;
import com.kwwsyk.endinv.common.options.ContentTransferMode;
import com.kwwsyk.endinv.fabric.ServerConfig;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

public final class PlayerEvents {

    private static final Set<UUID> PENDING_SYNC = new HashSet<>();

    private PlayerEvents() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(PlayerEvents::flushSync);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> scheduleSync(handler.player));
        ServerPlayerEvents.COPY_FROM.register(PlayerEvents::copyFromOldPlayer);
    }

    public static void markPlayersForSync(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            scheduleSync(player);
        }
    }

    private static void flushSync(MinecraftServer server) {
        Iterator<UUID> iterator = PENDING_SYNC.iterator();
        while (iterator.hasNext()) {
            UUID uuid = iterator.next();
            ServerPlayer player = server.getPlayerList().getPlayer(uuid);
            if (player != null) {
                sendInitialData(player);
                iterator.remove();
            }
        }
    }

    private static void scheduleSync(ServerPlayer player) {
        PENDING_SYNC.add(player.getUUID());
    }

    private static void copyFromOldPlayer(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) {
        if (alive) {
            return;
        }

        var uuidAttachment = ModRegistries.NbtAttachments.getEndInvUUID();
        UUID uuid = uuidAttachment.getWith(oldPlayer);
        uuidAttachment.setTo(newPlayer, uuid);

        SyncedConfig config = ModRegistries.NbtAttachments.getSyncedConfig().getWith(oldPlayer);
        ModRegistries.NbtAttachments.getSyncedConfig().setTo(newPlayer, config);
        ModInfo.getPacketDistributor().sendToPlayer(newPlayer, config);
        scheduleSync(newPlayer);
    }

    private static void sendInitialData(ServerPlayer player) {
        var configAttachment = ModRegistries.NbtAttachments.getSyncedConfig();
        SyncedConfig syncedConfig = configAttachment.getWith(player);
        ModInfo.getPacketDistributor().sendToPlayer(player, syncedConfig);

        // Broadcast effective menu attachability on join
        var serverCfg = ModInfo.getServerConfig();
        var menuCfg = serverCfg.specifiedMenuAttachability().get();
        boolean defaultAttach = serverCfg.enableAttaching().get();
        ModInfo.getPacketDistributor().sendToPlayer(player,
                new com.kwwsyk.endinv.common.network.payloads.toClient.MenuAttachabilityPayload(
                        defaultAttach,
                        menuCfg.isInventoryAttachable(),
                        menuCfg.getConfigs()
                ));

        if (ServerConfig.INSTANCE.transferMode().get() == ContentTransferMode.ALL) {
            ServerLevelEndInv.getEndInvForPlayer(player).ifPresent(endInv -> {
                ModInfo.getPacketDistributor().sendToPlayer(player, new EndInvContent(endInv.snapshotItemMap()));
                ModInfo.getPacketDistributor().sendToPlayer(player, EndInvMetadata.getWith(endInv));
            });
        }
    }
}
