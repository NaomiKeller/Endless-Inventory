package com.kwwsyk.endinv.common.client.gui.bg;

import com.kwwsyk.endinv.common.client.ClientModInfo;
import com.kwwsyk.endinv.common.client.gui.ScreenFramework;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public abstract class SFBgRendererImpl implements SFBgRenderer {

    protected final ScreenFramework frameWork;

    protected final int menuLeft;
    protected final int menuTop;
    protected final int pageLeft;
    protected final int pageTop;
    protected final AbstractContainerScreen<?> screen;
    public ScreenRectangleWidgetParam pageSwitchTabParam;
    protected final int imageWidth;

    protected final int rows;
    protected final int columns;

    //true for renderers used on the attached view's right-side tab column, so tab art/icon
    //placement designed for a left-side column (bevel facing right, toward the panel) gets
    //mirrored to face the correct direction on the other side instead.
    protected boolean mirrorTabs = false;

    public SFBgRendererImpl(ScreenFramework frameWork){
        this.frameWork = frameWork;
        this.screen = frameWork.screen;
        this.imageWidth = 256;
        this.rows = this.frameWork.rows();
        this.columns = this.frameWork.columns();
        this.menuLeft = ClientModInfo.containerScreenHelper.getGuiLeft(screen);
        this.menuTop = ClientModInfo.containerScreenHelper.getGuiTop(screen);
        this.pageLeft = frameWork.leftPos;
        this.pageTop = frameWork.topPos;
    }

    protected void renderPageBarContent(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY){
        int pageX = pageSwitchTabParam.XPos();
        int pageY = pageSwitchTabParam.YPos();
        for (int i = frameWork.firstPageIndex; i < frameWork.firstPageIndex+ frameWork.pageBarCount; ++i) {
            //fixed icon offset, matching vanilla's own advancement tabs: the icon doesn't
            //perfectly re-center between the unselected and (wider) selected sprite shapes, and
            //vanilla doesn't either - it keeps one offset regardless of state. Mirrored for the
            //attached view so it stays sensibly placed once the tab art itself is mirrored.
            int iconOffset = mirrorTabs ? 4 : 12;
            frameWork.getPages().get(i).renderPageIcon(guiGraphics, pageX + iconOffset, pageY + 5, partialTick);
            pageY += 28;
        }
        //the tab-name tooltip itself isn't drawn here - this whole method runs from renderBg(),
        //which happens before the item grid's own icons/counts render; a tooltip drawn here would
        //get painted over by them. See ScreenFramework#render for where it's actually drawn.
    }

    @Override
    public ScreenRectangleWidgetParam pageSwitchBarParam() {
        return pageSwitchTabParam;
    }

    @Override
    public ScreenFramework getScreenFrameWork(){
        return frameWork;
    }
}
