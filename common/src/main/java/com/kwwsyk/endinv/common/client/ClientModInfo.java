package com.kwwsyk.endinv.common.client;

import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.common.client.gui.EndInvSettingScreen;
import com.kwwsyk.endinv.common.client.gui.EndlessInventoryScreen;
import com.kwwsyk.endinv.common.client.option.ClientConfigs;
import com.kwwsyk.endinv.common.network.payloads.toServer.OpenEndInvPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.function.Function;

public class ClientModInfo {

    public static IInputHandler inputHandler;

    public static IContainerScreenHelper containerScreenHelper;

    private static java.util.function.Function<Screen, Screen> configScreenFactory;

    public static void sendOpenMenu(){
        int rows = ClientConfigs.EIM_CONFIG.Rows.get();
        if (rows <= 0) {
            rows = calculateDefaultRowsForMenu();
        }
        ModInfo.getPacketDistributor().sendToServer(new OpenEndInvPayload(true, rows));
    }

    private static int calculateDefaultRowsForMenu() {
        Minecraft mc = Minecraft.getInstance();
        int height = mc.getWindow().getGuiScaledHeight();
        return Math.max(Math.floorDiv(height - 60, 18) - 4, 1);
    }

    public static void setConfigScreenFactory(Function<Screen, Screen> factory) {
        configScreenFactory = factory;
    }

    public static Screen createConfigScreen(Screen parent) {
        if (configScreenFactory != null) {
            Screen screen = configScreenFactory.apply(parent);
            if (screen != null) {
                return screen;
            }
        }
        return parent instanceof EndlessInventoryScreen ? new EndInvSettingScreen.Menu(parent) : new EndInvSettingScreen.Attachment(parent);
    }
}
