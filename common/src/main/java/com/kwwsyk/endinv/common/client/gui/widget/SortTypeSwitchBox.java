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

    public ScreenFramework framework;
    private final int singleBoxHeight;
    private boolean isOpen;


    public SortTypeSwitchBox(ScreenFramework framework, int x, int y, int width, int height){
        super(x,y,width,height, Component.empty());
        this.framework = framework;
        this.singleBoxHeight = height;
    }

    public SortTypeSwitchBox(ScreenFramework framework, IRectangleParam sortTypeSwitchBoxParam){
        this(framework,
                sortTypeSwitchBoxParam.x(),
                sortTypeSwitchBoxParam.y(),
                sortTypeSwitchBoxParam.width(),
                sortTypeSwitchBoxParam.height()
        );
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
        this.height = open ? singleBoxHeight*(1+ SortType.values().length) : singleBoxHeight;
    }

    public void onClick(double mouseX,double mouseY){
        if(!isOpen){
            setOpen(true);
        }else {
            int y1 = getY()+singleBoxHeight;
            for(SortType type : SortType.values()){
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
            return true;
        }else return super.mouseClicked(mouseX,mouseY,button);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.pose().pushPose();
        guiGraphics.fill(getX(), getY(), getX() + width, getY() + singleBoxHeight, 0xff888888);
        guiGraphics.fill(getX() + 1, getY() + 1, getX() + width - 1, getY() + singleBoxHeight - 1, 0xff000000);
        if (isHoveringOnSingleBox(mouseY, getY()))
            guiGraphics.fillGradient(getX(), getY(), getX() + width, getY() + singleBoxHeight, 0x80ffffff, 0x80ffffff);
        SortType sortType = framework.sortType();
        String s = sortType.toString();
        guiGraphics.drawString(Minecraft.getInstance().font, s,getX()+2, getY() +2,0xffffffff);
        if(isOpen){
            guiGraphics.pose().translate(0, 0, 100.0F);
            int y1 = getY() +singleBoxHeight;
            for (SortType type : SortType.values()) {
                guiGraphics.fill(getX(), y1, getX() + width, y1 + singleBoxHeight, 0xff888888);
                guiGraphics.fill(getX() + 1, y1 + 1, getX() + width - 1, y1 + singleBoxHeight - 1, 0xff000000);
                if (isHoveringOnSingleBox(mouseY, y1)) {
                    guiGraphics.fillGradient(getX(), y1, getX() + width, y1 + singleBoxHeight, 0x80ffffff, 0x80ffffff);
                    guiGraphics.renderTooltip(
                            Minecraft.getInstance().font,
                            List.of(
                                    Component.translatable(type.translationKey).getVisualOrderText()
                            ),
                            mouseX,
                            mouseY
                    );
                }
                s = type.toString();
                guiGraphics.drawString(Minecraft.getInstance().font, s,getX()+2,y1+2,0xffffffff);
                y1+=singleBoxHeight;
            }
        }
        guiGraphics.pose().popPose();
    }
    private boolean isHoveringOnSingleBox(int mouseY,int minY){
        return mouseY>=minY && mouseY<=minY+singleBoxHeight && isHovered;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }
}
