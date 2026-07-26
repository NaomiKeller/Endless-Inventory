package com.kwwsyk.endinv.common.client.gui.page.slotView;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;

public interface SlotView extends Renderable {

    void renderSlotHighlightBack(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick);

    void renderSlotHighlightFront(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick);

    /**
     * Temporary SortBox-covered rendering path: draw the slot contents, but skip item decorations
     * such as count text and durability overlays because vanilla renders them at a high z level.
     */
    void renderWithoutDecorations(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick);
}
