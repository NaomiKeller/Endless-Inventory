package com.kwwsyk.endinv.neoforge.integrates.jei;

import com.kwwsyk.endinv.common.client.gui.AttachingScreen;
import com.kwwsyk.endinv.neoforge.client.events.ScreenAttachment;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;

import java.util.List;

public class AttachmentGuiHandler implements IGuiContainerHandler<AbstractContainerScreen<?>> {

    public AttachmentGuiHandler(){}

    @Override
    public List<Rect2i> getGuiExtraAreas(AbstractContainerScreen<?> containerScreen) {
        AttachingScreen<?> attachedScreen = ScreenAttachment.ATTACHMENT_MANAGER;
        if(attachedScreen!=null){
            return attachedScreen.getArea();
        }
        return IGuiContainerHandler.super.getGuiExtraAreas(containerScreen);
    }
}
