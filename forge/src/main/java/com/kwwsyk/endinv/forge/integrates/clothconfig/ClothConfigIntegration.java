package com.kwwsyk.endinv.forge.integrates.clothconfig;

import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModContainer;

public final class ClothConfigIntegration {

    private ClothConfigIntegration() {
    }

    public static void register(ModContainer container) {
        container.registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(ClothConfigScreenBuilder::create)
        );
    }
}
