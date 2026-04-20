package com.kwwsyk.endinv.common.client.gui.page.slotView;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;

public interface SlotView extends Renderable {

    void renderSlotHighlightBack(GuiGraphicsExtractor GuiGraphicsExtractor, int mouseX, int mouseY, float partialTick);

    void renderSlotHighlightFront(GuiGraphicsExtractor GuiGraphicsExtractor, int mouseX, int mouseY, float partialTick);
}

