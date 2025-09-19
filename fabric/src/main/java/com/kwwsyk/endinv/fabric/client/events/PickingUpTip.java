package com.kwwsyk.endinv.fabric.client.events;

import com.kwwsyk.endinv.common.client.event.AutoPickTipper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public final class PickingUpTip {

    private PickingUpTip() {
    }

    public static void register() {
        HudRenderCallback.EVENT.register((context, tickCounter) -> AutoPickTipper.onRenderGui(context));
    }
}
