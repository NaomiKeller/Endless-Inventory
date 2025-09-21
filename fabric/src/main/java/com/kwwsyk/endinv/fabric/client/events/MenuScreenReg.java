package com.kwwsyk.endinv.fabric.client.events;

import com.kwwsyk.endinv.common.ModRegistries;
import com.kwwsyk.endinv.common.client.gui.EndlessInventoryScreen;
import net.minecraft.client.gui.screens.MenuScreens;

public final class MenuScreenReg {

    private MenuScreenReg() {
    }

    public static void register() {
        MenuScreens.register(ModRegistries.Menus.getEndInvMenuType(), EndlessInventoryScreen::new);
    }
}
