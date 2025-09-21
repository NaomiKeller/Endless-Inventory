package com.kwwsyk.endinv.fabric.mixin;

import com.kwwsyk.endinv.fabric.client.events.ScreenAttachment;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiEventListener.class)
public interface GuiEventListenerMixin {

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void endinv$charTyped(char chr, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Screen screen) {
            if (ScreenAttachment.handleCharTyped(screen, chr, modifiers)) {
                cir.setReturnValue(true);
            }
        }
    }
}
