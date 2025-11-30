package com.kwwsyk.endinv.common.client.option;


import com.kwwsyk.endinv.common.client.gui.bg.IRectangleParam;

public class PageSwitchBarConfig {

    public record Param(int maxBars, int xOffset, int yOffset, boolean direction_isVertical,
                        IRectangleParam buttonDec, IRectangleParam buttonInc) {
    }


}
