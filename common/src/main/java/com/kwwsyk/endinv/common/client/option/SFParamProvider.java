package com.kwwsyk.endinv.common.client.option;

import com.kwwsyk.endinv.common.client.gui.bg.IRectangleParam;
import com.kwwsyk.endinv.common.menu.page.PageType;

import java.util.List;

public interface SFParamProvider {

    int rows();
    int columns();
    int leftPos();
    int topPos();

    TextureMode textureMode();

    List<PageType> pages();

    /*
    * Absolute coordinates
     */
    IRectangleParam pageParamAdjusted();

    PageSwitchBarConfig.Param pageTabParamAdjusted();
    IRectangleParam pageTabDecA();
    IRectangleParam pageTabIncA();

    IRectangleParam sortBoxA();
    IRectangleParam configButtonA();
    IRectangleParam reverseSortButtonA();
    IRectangleParam searchBoxA();
}
