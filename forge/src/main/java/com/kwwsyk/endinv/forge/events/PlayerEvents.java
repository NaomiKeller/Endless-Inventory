package com.kwwsyk.endinv.forge.events;

import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.common.ModRegistries;
import com.kwwsyk.endinv.common.ServerLevelEndInv;
import com.kwwsyk.endinv.common.network.payloads.toClient.EndInvContent;
import com.kwwsyk.endinv.common.network.payloads.toClient.EndInvMetadata;
import com.kwwsyk.endinv.common.options.ContentTransferMode;
import com.kwwsyk.endinv.forge.ServerConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModInfo.MOD_ID)
public class PlayerEvents {

    public static boolean tickRefresh =true;
    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event){
        if(event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer serverPlayer){
            if(tickRefresh) {
                ModInfo.getPacketDistributor().sendToPlayer(serverPlayer, ModRegistries.NbtAttachments.getSyncedConfig().computeIfAbsent(serverPlayer));
                if(ServerConfig.CONFIG.TRANSFER_MODE.get()== ContentTransferMode.ALL){
                    ServerLevelEndInv.getEndInvForPlayer(serverPlayer).ifPresent(endInv -> {
                        ModInfo.getPacketDistributor().sendToPlayer(serverPlayer,new EndInvContent(endInv.getItemMap()));
                        ModInfo.getPacketDistributor().sendToPlayer(serverPlayer,EndInvMetadata.getWith(endInv));
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
