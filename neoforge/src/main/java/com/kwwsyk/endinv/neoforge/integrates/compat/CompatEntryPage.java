package com.kwwsyk.endinv.neoforge.integrates.compat;

import com.kwwsyk.endinv.common.client.gui.ScreenFramework;
import com.kwwsyk.endinv.common.client.gui.page.ItemEntryDisplay;
import com.kwwsyk.endinv.common.menu.page.PageType;
import com.kwwsyk.endinv.common.util.SortType;

import java.util.List;

public class CompatEntryPage extends ItemEntryDisplay {

    public CompatEntryPage(PageType pageType, ScreenFramework screenFramework, DescriptionProvider descriptionProvider, List<SortType> sortTypes) {
        super(pageType, screenFramework, descriptionProvider);
        this.availableSorts = sortTypes;
    }
}
