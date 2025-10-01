package com.kwwsyk.endinv.fabric.integrates.clothconfig;

import com.kwwsyk.endinv.common.client.ClientModInfo;

public final class ClothConfigIntegration {

    private ClothConfigIntegration() {
    }

    public static void register() {
        ClientModInfo.setConfigScreenFactory(ClothConfigScreenBuilder::create);
    }
}
