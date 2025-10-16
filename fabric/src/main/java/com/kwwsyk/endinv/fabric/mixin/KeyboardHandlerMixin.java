package com.kwwsyk.endinv.fabric.mixin;

import com.kwwsyk.endinv.fabric.client.events.ScreenCharTypedEvents;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {

    @Inject(
        method = "method_1458(Lnet/minecraft/client/gui/components/events/GuiEventListener;II)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/events/GuiEventListener;charTyped(CI)Z",
            ordinal = 0
        ),
        cancellable = true
    )
    private static void endinv$beforeCharTyped(GuiEventListener guiEventListener, int codePoint, int modifiers, CallbackInfo ci) {
        endinv$beforeCharTypedInternal(guiEventListener, (char) codePoint, modifiers, ci);
    }

    @Inject(
        method = "method_1473(Lnet/minecraft/client/gui/components/events/GuiEventListener;CI)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/events/GuiEventListener;charTyped(CI)Z",
            ordinal = 0
        ),
        cancellable = true
    )
    private static void endinv$beforeCharTyped2(GuiEventListener guiEventListener, char codePoint, int modifiers, CallbackInfo ci) {
        endinv$beforeCharTypedInternal(guiEventListener, codePoint, modifiers, ci);
    }

    private static void endinv$beforeCharTypedInternal(GuiEventListener guiEventListener, char codePoint, int modifiers, CallbackInfo ci) {
        if (ci.isCancelled()) {
            return;
        }
        if (ScreenCharTypedEvents.BEFORE_CHAR_TYPED.invoker().beforeCharTyped(guiEventListener, codePoint, modifiers)) {
            ci.cancel();
        }
    }
}
