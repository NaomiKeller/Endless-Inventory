package com.kwwsyk.endinv.common.client.gui.page.slotView;

import com.kwwsyk.endinv.common.client.gui.page.ItemEntryDisplay;
import com.kwwsyk.endinv.common.util.ItemKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

public class ItemEntryPageSlotView extends ItemPageSlotView {

    public ItemEntryPageSlotView(EntryPageViewContainer container, @Nullable ItemKey key, int slot, int x, int y) {
        super(container, key, slot, x, y, container.page.framework.columns() * 18, 18);
    }

    /**
     * Renders the graphical user interface (GUI) element.
     *
     * @param guiGraphics the GuiGraphics object used for rendering.
     * @param mouseX      the x-coordinate of the mouse cursor.
     * @param mouseY      the y-coordinate of the mouse cursor.
     * @param partialTick the partial tick time.
     */
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        var view = ((EntryPageViewContainer) container);
        guiGraphics.drawString(
                Minecraft.getInstance().font,
                view.entryProvider.apply((ItemEntryDisplay) view.page, get()),
                x + 18, y + 5, 0xFFFFFFFF, true
        );

    }
}

