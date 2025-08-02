package com.kwwsyk.endinv.forge.client.events;

import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.common.ModRegistries;
import com.kwwsyk.endinv.common.client.gui.EndlessInventoryScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterMenuScreensEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD,modid = ModInfo.MOD_ID,value = Dist.CLIENT)
public class MenuScreenReg {
    @SubscribeEvent
    public static void reg(RegisterMenuScreensEvent event){
        event.register(ModRegistries.Menus.getEndInvMenuType(), EndlessInventoryScreen::new);
    }
}
