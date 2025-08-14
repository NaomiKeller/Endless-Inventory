package com.kwwsyk.endinv.forge.integrates.jei;

import com.kwwsyk.endinv.common.client.gui.AttachedScreen;
import com.kwwsyk.endinv.forge.client.events.ScreenAttachment;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;

import java.util.List;

public class AttachmentGuiHandler implements IGuiContainerHandler<AbstractContainerScreen<?>> {

    public AttachmentGuiHandler(){}

    @Override
    public List<Rect2i> getGuiExtraAreas(AbstractContainerScreen<?> containerScreen) {
        AttachedScreen<?> attachedScreen = ScreenAttachment.ATTACHMENT_MANAGER;
        if(attachedScreen!=null){
            return attachedScreen.getArea();
        }
        return IGuiContainerHandler.super.getGuiExtraAreas(containerScreen);
    }
}
