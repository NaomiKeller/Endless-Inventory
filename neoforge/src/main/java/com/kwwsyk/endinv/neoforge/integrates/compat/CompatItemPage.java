package com.kwwsyk.endinv.neoforge.integrates.compat;

import com.kwwsyk.endinv.common.client.gui.ScreenFramework;
import com.kwwsyk.endinv.common.client.gui.page.ItemDisplay;
import com.kwwsyk.endinv.common.menu.page.PageType;
import com.kwwsyk.endinv.common.util.SortType;

import java.util.List;

public class CompatItemPage extends ItemDisplay {

    public CompatItemPage(PageType pageType, ScreenFramework screenFramework, List<SortType> sortTypes) {
        super(pageType, screenFramework);
        this.availableSorts = sortTypes;
    }
}
