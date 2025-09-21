package com.kwwsyk.endinv.fabric.client.events;

public final class ClientEvents {

    private ClientEvents() {
    }

    public static void register() {
        MenuScreenReg.register();
        KeyMappingTrigger.register();
        PickingUpTip.register();
        ScreenAttachment.register();
        ScreenDebug.register();
    }
}
