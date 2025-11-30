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
    int pageTabCount();
    IRectangleParam pageParam();

    IRectangleParam pageTabDec();
    IRectangleParam pageTabInc();

    IRectangleParam sortBox();
    IRectangleParam configButton();
    IRectangleParam reverseSortButton();
    IRectangleParam searchBox();
}
