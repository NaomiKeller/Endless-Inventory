package com.kwwsyk.endinv.common.client.option;

import com.kwwsyk.endinv.common.client.gui.bg.IRectangleParam;
import com.kwwsyk.endinv.common.menu.page.PageTypeRegistry;
import com.kwwsyk.endinv.common.options.config.ComplexConfigEntryImpl;
import com.kwwsyk.endinv.common.options.config.ConfigEntryImpl;
import com.kwwsyk.endinv.common.options.config.EntryPresentable;

public class AttachedMenuConfig extends ComplexConfigEntryImpl<AttachedMenuOptions> {
    
    public static final AttachedMenuConfig INSTANCE = new AttachedMenuConfig("Attached Screen(Menu) Options", new String[]{"Attached Menu Options"});
    
    public final EnumEntry<AttachedMenuOptions.BasicLayout> BasicLayout = new EnumEntry<>(
            "BasicLayout", new String[]{"The basic layout of attached screen."}, AttachedMenuOptions.BasicLayout.LEFT);
    public final IntEntry LeftPos = new IntEntry(
            "LeftPos", new String[]{"The left position of attached screen."}, 20, Integer.MIN_VALUE, Integer.MAX_VALUE);
    public final IntEntry TopPos = new IntEntry(
            "TopPos", new String[]{"The top position of attached screen."}, 20, Integer.MIN_VALUE, Integer.MAX_VALUE);
    public final PageBasicLayoutConfig PageBasicLayout = new PageBasicLayoutConfig(
            "PageBasicLayout", new String[]{"The basic layout of page."}, PageBasicLayoutConfig.Param.DEFAULT);
    public final ListEntry<String> DontDisplayPages = new ListEntry<>(
            "HidePages",
            new String[]{"The pages will not have a tab in the page switch bar to be switched to"},
            PageTypeRegistry.getIdList()
    );
    public final EnumEntry<TextureMode> TextureMode = new EnumEntry<>(
            "TextureMode",
            EntryPresentable.generateComments(TextureMode.class, "Controls the texture of Screen's widgets."),
            com.kwwsyk.endinv.common.client.option.TextureMode.FROM_RESOURCE
    );
    public final ListEntry<Integer> PageRectangle = IRectangleParam.createRectangleConfigEntry(
            "PageRectangleParam", new String[]{"The rectangle of page and is active when TextureMode is Transparent"}, AttachedMenuOptions.PAGE_RECTANGLE
    );//todo detailed description
    
    public final PageSwitchBarConfig PageSwitchBar = PageSwitchBarConfig.INSTANCE;
    
    public final ListEntry<Integer> SearchBoxParam = IRectangleParam.createRectangleConfigEntry(
            "SearchBoxParam", new String[]{"The search box parameters"}, AttachedMenuOptions.SEARCH_BOX);
    public final ListEntry<Integer> SortBoxParam = IRectangleParam.createRectangleConfigEntry(
            "SortBoxParam", new String[]{"The sort box parameters"}, AttachedMenuOptions.SORT_BOX);
    public final ListEntry<Integer> ReverseSortButtonParam = IRectangleParam.createRectangleConfigEntry(
            "ReverseSortButtonParam", new String[]{"The reverse sort button parameters"}, AttachedMenuOptions.REVERSE_SORT_BUTTON);
    public final ListEntry<Integer> ConfigButtonParam = IRectangleParam.createRectangleConfigEntry(
            "ConfigButtonParam", new String[]{"The config button parameters"}, AttachedMenuOptions.CONFIG_BUTTON);
    
    
    public AttachedMenuConfig(String key, String[] comments) {
        super(key, comments, AttachedMenuOptions.DEFAULT);
    }
    
    @Override
    public AttachedMenuOptions get() {
        return new AttachedMenuOptions(
                BasicLayout.get(),
                PageBasicLayout.get(),
                PageTypeRegistry.displayingPages(DontDisplayPages.get()),
                PageSwitchBar.get(),
                LeftPos.get(), TopPos.get(),
                TextureMode.get(),
                IRectangleParam.fromConfigEntry(PageRectangle),
                IRectangleParam.fromConfigEntry(SearchBoxParam),
                IRectangleParam.fromConfigEntry(SortBoxParam),
                IRectangleParam.fromConfigEntry(ConfigButtonParam),
                IRectangleParam.fromConfigEntry(ReverseSortButtonParam)
        );
    }
    
    @Override
    public void set(AttachedMenuOptions attachedMenuOptions) {
        BasicLayout.set(attachedMenuOptions.basicLayout);
        PageBasicLayout.set(attachedMenuOptions.pageParam);
        DontDisplayPages.set(PageTypeRegistry.dontDisplayPages(attachedMenuOptions.pages()));
        PageSwitchBar.set(attachedMenuOptions.pageSwitchBarParam);
        LeftPos.set(attachedMenuOptions.leftPos());
        TopPos.set(attachedMenuOptions.topPos());
        TextureMode.set(attachedMenuOptions.textureMode());
        PageRectangle.set(IRectangleParam.toList(attachedMenuOptions.pageRectangleParam));
        SearchBoxParam.set(IRectangleParam.toList(attachedMenuOptions.searchBoxParam));
        SortBoxParam.set(IRectangleParam.toList(attachedMenuOptions.sortBoxParam));
        ConfigButtonParam.set(IRectangleParam.toList(attachedMenuOptions.configButtonParam));
        ReverseSortButtonParam.set(IRectangleParam.toList(attachedMenuOptions.reverseSortButtonParam));
    }

    @Override
    public ConfigEntryImpl<?>[] fields() {
        return new ConfigEntryImpl[]{
                BasicLayout,
                LeftPos, TopPos,
                PageBasicLayout,
                DontDisplayPages,
                TextureMode,
                PageRectangle,
                PageSwitchBar,
                SearchBoxParam, SortBoxParam, ReverseSortButtonParam, ConfigButtonParam
        };
    }
}
