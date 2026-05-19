package com.kwwsyk.endinv.common.event;

import com.kwwsyk.endinv.common.ModRegistries;
import com.kwwsyk.endinv.common.ServerLevelEndInv;
import com.kwwsyk.endinv.common.network.payloads.toClient.EndInvContent;
import com.kwwsyk.endinv.common.network.payloads.toClient.EndInvMetadata;
import com.kwwsyk.endinv.common.network.payloads.toClient.MenuAttachabilityPayload;
import com.kwwsyk.endinv.common.options.ContentTransferMode;
import com.kwwsyk.endinv.common.options.ServerConfigs;
import net.minecraft.server.level.ServerPlayer;

import static com.kwwsyk.endinv.common.ModInfo.getPacketDistributor;

public class PlayerEventHelper {

    public static void sendData(ServerPlayer serverPlayer){
        getPacketDistributor().sendToAllPlayer(ModRegistries.NbtAttachments.getSyncedConfig().computeIfAbsent(serverPlayer));
        // Send effective menu attachability
        var menuCfg = ServerConfigs.SPECIFIED_ATTACHABILITY.get();
        boolean defaultAttach = ServerConfigs.DEFAULT_ATTACH.get();
        getPacketDistributor().sendToAllPlayer(
                new MenuAttachabilityPayload(
                        defaultAttach,
                        menuCfg.isInventoryAttachable(),
                        menuCfg.getConfigs()
                ));
        if(ServerConfigs.ENDINV_BEHAVIOR.TransferMode.get()== ContentTransferMode.ALL){
            ServerLevelEndInv.getEndInvForPlayer(serverPlayer).ifPresent(endInv -> {
                getPacketDistributor().sendToAllPlayer(new EndInvContent(endInv.getItemMap()));
                getPacketDistributor().sendToAllPlayer(EndInvMetadata.getWith(endInv));
            });
        }
    }
}
