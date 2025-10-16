package com.kwwsyk.endinv.fabric.client.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.components.events.GuiEventListener;

public final class ScreenCharTypedEvents {

    public static final Event<BeforeCharTyped> BEFORE_CHAR_TYPED = EventFactory.createArrayBacked(
        BeforeCharTyped.class,
        listeners -> (guiEventListener, codePoint, modifiers) -> {
            for (BeforeCharTyped listener : listeners) {
                if (listener.beforeCharTyped(guiEventListener, codePoint, modifiers)) {
                    return true;
                }
            }
            return false;
        }
    );

    @FunctionalInterface
    public interface BeforeCharTyped {
        boolean beforeCharTyped(GuiEventListener guiEventListener, char codePoint, int modifiers);
    }

    private ScreenCharTypedEvents() {
    }
}
