package com.kwwsyk.endinv.fabric.mixin;

import com.kwwsyk.endinv.fabric.client.events.ScreenAttachment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void endinv$mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> cir) {
        if (ScreenAttachment.handleMouseDrag((AbstractContainerScreen<?>) (Object) this, mouseX, mouseY, button, deltaX, deltaY)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void endinv$renderTail(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        // Fallback: if we cannot pinpoint renderBg reliably across mappings, draw both phases at TAIL.
        ScreenAttachment.onRenderAfterBackground((AbstractContainerScreen<?>) (Object) this, graphics, mouseX, mouseY, partialTick);
        ScreenAttachment.onRenderPost((AbstractContainerScreen<?>) (Object) this, graphics, mouseX, mouseY, partialTick);
    }
}
