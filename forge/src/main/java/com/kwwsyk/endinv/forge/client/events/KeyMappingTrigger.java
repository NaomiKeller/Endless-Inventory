package com.kwwsyk.endinv.forge.client.events;

import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.common.network.payloads.SyncedConfig;
import com.kwwsyk.endinv.common.network.payloads.toServer.OpenEndInvPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientTickEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.kwwsyk.endinv.forge.ClientModInitializer.OPEN_MENU_KEY;

@Mod.EventBusSubscriber(value = Dist.CLIENT,modid = ModInfo.MOD_ID)
public class KeyMappingTrigger {

    @SubscribeEvent
    public static void keyPressed(ClientTickEvent event){
        if(event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        while (OPEN_MENU_KEY.get().consumeClick()) {
            SyncedConfig.readAndSyncClientConfigToServer(true);
            ModInfo.getPacketDistributor().sendToServer(new OpenEndInvPayload(true));
        }
    }
}
