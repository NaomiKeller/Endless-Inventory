package com.kwwsyk.endinv.fabric.mixin;

import com.kwwsyk.endinv.fabric.client.events.ScreenAttachment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void endinv$mouseDragged(MouseButtonEvent event, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> cir) {
        if (ScreenAttachment.handleMouseDrag((AbstractContainerScreen<?>) (Object) this, event, deltaX, deltaY)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "extractContents", at = @At("HEAD"))
    private void endinv$renderHead(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        // Render before the screen widget layer so attached-page content does not cover widget tooltips.
        ScreenAttachment.onRenderAfterBackground((AbstractContainerScreen<?>) (Object) this, graphics, mouseX, mouseY, partialTick);
        ScreenAttachment.onRenderPost((AbstractContainerScreen<?>) (Object) this, graphics, mouseX, mouseY, partialTick);
    }
}
