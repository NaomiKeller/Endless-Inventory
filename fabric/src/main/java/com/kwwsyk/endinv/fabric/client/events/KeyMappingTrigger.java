package com.kwwsyk.endinv.fabric.client.events;

import com.kwwsyk.endinv.common.client.ClientModInfo;
import com.kwwsyk.endinv.common.client.KeyMappings;
import com.kwwsyk.endinv.fabric.ClientModInit;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.player.LocalPlayer;

public final class KeyMappingTrigger {

    private KeyMappingTrigger() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            LocalPlayer player = client.player;
            if (player == null) {
                return;
            }
            var key = ClientModInit.getRegisteredKey(KeyMappings.OPEN_MENU);
            while (key.consumeClick()) {
                ClientModInfo.sendOpenMenu();
            }
        });
    }
}
