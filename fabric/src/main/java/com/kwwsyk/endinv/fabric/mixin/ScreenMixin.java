package com.kwwsyk.endinv.fabric.mixin;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Screen.class)
public interface ScreenMixin extends com.kwwsyk.endinv.common.mixin.ScreenMixin {

    @Override
    @Invoker("removeWidget")
    void endinv$invokeRemoveWidget(GuiEventListener widget);
}

