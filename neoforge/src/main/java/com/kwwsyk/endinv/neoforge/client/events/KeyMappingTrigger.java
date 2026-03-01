package com.kwwsyk.endinv.neoforge.client.events;

import com.kwwsyk.endinv.common.AbstractClientModInitializer;
import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.common.client.ClientModInfo;
import com.kwwsyk.endinv.common.client.KeyMappings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(value = Dist.CLIENT,modid = ModInfo.MOD_ID)
public class KeyMappingTrigger {

    @SubscribeEvent
    public static void keyPressed(ClientTickEvent.Post event){
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        if (AbstractClientModInitializer.ENDINV_CLIENT != null) {
            while (AbstractClientModInitializer.ENDINV_CLIENT.KEY_MAPPING_MAP.get(KeyMappings.OPEN_MENU).consumeClick()) {
                ClientModInfo.sendOpenMenu();
            }
        }
    }
}
