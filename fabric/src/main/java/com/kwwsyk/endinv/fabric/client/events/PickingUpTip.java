package com.kwwsyk.endinv.fabric.client.events;

import com.kwwsyk.endinv.common.client.event.AutoPickTipper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.Identifier;

public final class PickingUpTip {

    private PickingUpTip() {
    }

    public static void register() {
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MISC_OVERLAYS,
                Identifier.fromNamespaceAndPath("endless_inventory", "pickup_tip"),
                (graphics, deltaTracker) -> AutoPickTipper.onRenderGui(graphics)
        );
    }
}
