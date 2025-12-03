package com.kwwsyk.endinv.neoforge.events;

import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.common.ModRegistries;
import com.kwwsyk.endinv.common.ServerLevelEndInv;
import com.kwwsyk.endinv.common.network.payloads.toClient.EndInvContent;
import com.kwwsyk.endinv.common.network.payloads.toClient.EndInvMetadata;
import com.kwwsyk.endinv.common.network.payloads.toClient.MenuAttachabilityPayload;
import com.kwwsyk.endinv.common.options.ContentTransferMode;
import com.kwwsyk.endinv.common.options.ServerConfigs;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = ModInfo.MOD_ID)
public class PlayerEvents {

    public static boolean tickRefresh =true;
    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event){
        if(event.getEntity() instanceof ServerPlayer serverPlayer){
            if(tickRefresh) {
                PacketDistributor.sendToPlayer(serverPlayer, ModRegistries.NbtAttachments.getSyncedConfig().computeIfAbsent(serverPlayer));
                // Send effective menu attachability
                var menuCfg = com.kwwsyk.endinv.common.options.ServerConfigs.SPECIFIED_ATTACHABILITY.get();
                boolean defaultAttach = com.kwwsyk.endinv.common.options.ServerConfigs.DEFAULT_ATTACH.get();
                PacketDistributor.sendToPlayer(serverPlayer,
                        new MenuAttachabilityPayload(
                                defaultAttach,
                                menuCfg.isInventoryAttachable(),
                                menuCfg.getConfigs()
                        ));
                if(ServerConfigs.ENDINV_BEHAVIOR.TransferMode.get()== ContentTransferMode.ALL){
                    ServerLevelEndInv.getEndInvForPlayer(serverPlayer).ifPresent(endInv -> {
                        PacketDistributor.sendToPlayer(serverPlayer,new EndInvContent(endInv.getItemMap()));
                        PacketDistributor.sendToPlayer(serverPlayer,EndInvMetadata.getWith(endInv));
                    });
                }
                tickRefresh = false ;
            }
        }
    }

    @SubscribeEvent
    public static void onRespawnClone(PlayerEvent.Clone event){
        tickRefresh=true;
    }

    @SubscribeEvent
    public static void onJoinLevel(PlayerEvent.PlayerLoggedInEvent event){
        tickRefresh = true;
    }
}
