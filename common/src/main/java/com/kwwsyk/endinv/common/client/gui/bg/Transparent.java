package com.kwwsyk.endinv.common.client.gui.bg;

import com.kwwsyk.endinv.common.client.gui.ScreenFramework;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class Transparent extends SFBgRendererImpl {

    private static final int PAGE_FRAME_COLOR = 0x80A0A0A0;
    private static final int PAGE_BG_COLOR = 0x30373737;

    public Transparent(ScreenFramework frameWork, IRectangleParam pageSwitchTabParam) {
        super(frameWork);
        this.pageSwitchTabParam = pageSwitchTabParam;
    }


    public class GridPageRenderer implements PageBgRender {

        @Override
        public void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
            int startX = pageLeft;
            int startY = pageTop;
            int gridWidth = 18*columns;
            int gridHeight = 18*rows;
            int leftWidth = 8;
            int rightWidth = 8;
            int pageWidth = leftWidth+gridWidth+rightWidth;
            int topHeight = 17;
            int bottomHeight = 12;
            int pageHeight = topHeight+gridHeight+bottomHeight;

            guiGraphics.fill(startX,startY,startX+pageWidth,startY+topHeight,PAGE_FRAME_COLOR);
            guiGraphics.fill(startX,startY+topHeight,startX+leftWidth,startY+pageHeight,PAGE_FRAME_COLOR);
            guiGraphics.fill(startX+leftWidth+gridWidth,startY+topHeight,startX+pageWidth,startY+pageHeight,PAGE_FRAME_COLOR);
            guiGraphics.fill(startX+leftWidth,startY+topHeight+gridHeight,startX+leftWidth+gridWidth,startY+pageHeight,PAGE_FRAME_COLOR);

            guiGraphics.fill(startX+leftWidth,startY+topHeight,startX+leftWidth+gridWidth,startY+topHeight+gridHeight,PAGE_BG_COLOR);
        }
    }

    @Override
    public void renderBg(@NotNull GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
    }

    @Override
    public Optional<PageBgRender> getDefaultPageBgRenderer() {
        return Optional.of(new GridPageRenderer());
    }
}
