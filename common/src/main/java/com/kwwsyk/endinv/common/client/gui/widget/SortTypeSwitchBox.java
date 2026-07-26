package com.kwwsyk.endinv.common.client.gui.widget;

import com.kwwsyk.endinv.common.client.gui.ScreenFramework;
import com.kwwsyk.endinv.common.client.gui.bg.IRectangleParam;
import com.kwwsyk.endinv.common.util.SortType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.List;

public class SortTypeSwitchBox extends AbstractWidget {

    private static final int DROPDOWN_Z_OFFSET = 200;

    public ScreenFramework framework;
    private final int singleBoxHeight;
    private List<SortType> sortTypes;
    private boolean isOpen;


    public SortTypeSwitchBox(ScreenFramework framework, int x, int y, int width, int height, List<SortType> sortTypes){
        super(x,y,width,height, Component.empty());
        this.framework = framework;
        this.singleBoxHeight = height;
        this.sortTypes = sortTypes;
    }

    public SortTypeSwitchBox(ScreenFramework framework, IRectangleParam sortTypeSwitchBoxParam, List<SortType> sortTypes){
        this(framework,
                sortTypeSwitchBoxParam.x(),
                sortTypeSwitchBoxParam.y(),
                sortTypeSwitchBoxParam.width(),
                sortTypeSwitchBoxParam.height(),
                sortTypes
        );
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
        this.height = open ? singleBoxHeight*(1+ sortTypes.size()) : singleBoxHeight;
    }

    public void setSortTypes(List<SortType> sortTypes) {
        this.sortTypes = sortTypes;
        setOpen(false);
    }

    public void onClick(double mouseX,double mouseY){
        if(!isOpen){
            setOpen(true);
        }else {
            int y1 = getY()+singleBoxHeight;
            for(SortType type : sortTypes){
                if(isHoveringOnSingleBox((int) mouseY,y1)){
                    framework.switchSortTypeTo(type);
                    return;
                }
                y1+= singleBoxHeight;
            }
            setOpen(false);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button){
        if(active && visible && !this.clicked(mouseX,mouseY) && isOpen){
            setOpen(false);
            framework.consumeNextMouseRelease();
            return true;
        }
        boolean clicked = super.mouseClicked(mouseX,mouseY,button);
        if(clicked) {
            framework.consumeNextMouseRelease();
        }
        return clicked;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(getX(), getY(), getX() + width, getY() + singleBoxHeight, 0xff888888);
        guiGraphics.fill(getX() + 1, getY() + 1, getX() + width - 1, getY() + singleBoxHeight - 1, 0xff000000);
        if (isHoveringOnSingleBox(mouseY, getY()))
            guiGraphics.fillGradient(getX(), getY(), getX() + width, getY() + singleBoxHeight, 0x80ffffff, 0x80ffffff);
        SortType sortType = framework.sortType();
        String s = sortType.toString();
        renderScrollingString(guiGraphics, Minecraft.getInstance().font, Component.literal(s),
                getX(),getY(), getX()+width,getY()+singleBoxHeight,0xffffffff);
        if(isOpen){
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, DROPDOWN_Z_OFFSET);
            int y1 = getY() +singleBoxHeight;
            for (SortType type : sortTypes) {
                guiGraphics.fill(getX(), y1, getX() + width, y1 + singleBoxHeight, 0xff888888);
                guiGraphics.fill(getX() + 1, y1 + 1, getX() + width - 1, y1 + singleBoxHeight - 1, 0xff000000);
                if (isHoveringOnSingleBox(mouseY, y1)) {
                    guiGraphics.fillGradient(getX(), y1, getX() + width, y1 + singleBoxHeight, 0x80ffffff, 0x80ffffff);
                }
                s = type.toString();
                renderScrollingString(guiGraphics, Minecraft.getInstance().font, Component.literal(s),
                        getX(),y1, getX()+width,y1+singleBoxHeight,0xffffffff);
                y1+=singleBoxHeight;
            }
            guiGraphics.pose().popPose();
        }
    }

    public void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if(!visible || !isOpen) return;
        int y1 = getY() +singleBoxHeight;
        for (SortType type : sortTypes) {
            if (isHoveringOnSingleBox(mouseY, y1)) {
                guiGraphics.renderTooltip(
                        Minecraft.getInstance().font,
                        List.of(
                                Component.translatable(type.translationKey).getVisualOrderText()
                        ),
                        mouseX,
                        mouseY
                );
                return;
            }
            y1+=singleBoxHeight;
        }
    }

    public boolean covers(int x, int y, int width, int height) {
        return visible && isOpen
                && x < getX() + this.width
                && x + width > getX()
                && y < getY() + this.height
                && y + height > getY();
    }

    private boolean isHoveringOnSingleBox(int mouseY,int minY){
        return mouseY>=minY && mouseY<=minY+singleBoxHeight && isHovered;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }
}
