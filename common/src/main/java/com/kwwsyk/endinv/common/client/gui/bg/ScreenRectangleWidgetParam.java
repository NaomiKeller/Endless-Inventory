package com.kwwsyk.endinv.common.client.gui.bg;

public record ScreenRectangleWidgetParam(int XPos,int YPos,int XSize,int YSize) implements IRectangleParam {

    @Override
    public boolean hasClickedOn(int mouseX, int mouseY){
        return mouseX>=XPos && mouseX<=XPos + XSize && mouseY>=YPos && mouseY<= YPos + YSize;
    }
}
