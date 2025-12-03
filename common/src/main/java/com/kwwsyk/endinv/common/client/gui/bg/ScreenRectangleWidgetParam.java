package com.kwwsyk.endinv.common.client.gui.bg;

public record ScreenRectangleWidgetParam(int x, int y, int width, int height) implements IRectangleParam {

    @Override
    public boolean hasClickedOn(int mouseX, int mouseY){
        return mouseX>= x && mouseX<= x + width && mouseY>= y && mouseY<= y + height;
    }
}
