package com.kwwsyk.endinv.common.client.option;

import com.kwwsyk.endinv.common.client.gui.bg.IRectangleParam;
import com.kwwsyk.endinv.common.client.gui.bg.ScreenRectangleWidgetParam;
import com.kwwsyk.endinv.common.menu.page.PageType;
import com.kwwsyk.endinv.common.options.config.EntryPresentable;

import java.util.List;

public class AttachedMenuOptions implements SFParamProvider{

    public enum BasicLayout implements EntryPresentable {
        LEFT("Place at left side of screen"),
        RIGHT("Place at right side of screen"),
        MERGE_JEI("Place at right side of screen and merge with JEI, if no Jei, it will fall back to RIGHT mode."),
        FULL_CUSTOMIZE("Full customize the place of the menu.");

        private final String description;

        BasicLayout(String description){
            this.description = description;
        }

        @Override
        public String description() { return description; }
    }

    private final BasicLayout basicLayout;

    private final PageBasicLayoutConfig.Param pageParam;
    private final List<PageType> displayPages;

    private final PageSwitchBarConfig.Param pageSwitchBarParam;

    private final int leftPos;
    private final int topPos;

    private final TextureMode textureMode;
    private final IRectangleParam pageRectangleParam;

    private final IRectangleParam searchBoxParam;
    private final IRectangleParam sortBoxParam;
    private final IRectangleParam configButtonParam;
    private final IRectangleParam reverseSortButtonParam;

    public AttachedMenuOptions(
            BasicLayout basicLayout,
            PageBasicLayoutConfig.Param pageParam,
            List<PageType> displayPages,
            PageSwitchBarConfig.Param pageSwitchBarParam,
            int leftPos, int topPos,
            TextureMode textureMode,
            IRectangleParam pageRectangleParam,
            IRectangleParam searchBoxParam,
            IRectangleParam sortBoxParam,
            IRectangleParam configButtonParam,
            IRectangleParam reverseSortButtonParam
    ) {
        this.basicLayout = basicLayout;
        this.pageParam = pageParam;
        this.displayPages = displayPages;
        this.pageSwitchBarParam = pageSwitchBarParam;
        this.leftPos = leftPos;
        this.topPos = topPos;
        this.textureMode = textureMode;
        this.pageRectangleParam = pageRectangleParam;
        this.searchBoxParam = searchBoxParam;
        this.sortBoxParam = sortBoxParam;
        this.configButtonParam = configButtonParam;
        this.reverseSortButtonParam = reverseSortButtonParam;
    }

    public static AttachedMenuOptions ofLeft(
            int rows, int columns,
            boolean autoRows, boolean autoColumns,
            List<PageType> displayPages,
            int maxPageBar,
            PageSwitchBarConfig.Param pageSwitchBarParam,
            int leftPos, int topPos,
            TextureMode textureMode,
            IRectangleParam pageRectangleParam
    ){
        int imageWidth = 13 +18*columns;
        int searchBoxY = topPos + 17 + 18 * rows + 12;
        return new AttachedMenuOptions(
                BasicLayout.LEFT,
                new PageBasicLayoutConfig.Param(rows, columns, 17, 8, autoRows, autoColumns),
                displayPages,
                pageSwitchBarParam,
                leftPos,topPos,
                textureMode,
                pageRectangleParam,
                new ScreenRectangleWidgetParam(
                        leftPos + 1,
                        searchBoxY,
                        Math.min(200, imageWidth),
                        Math.min(20, 31 - topPos)
                ),
                new ScreenRectangleWidgetParam(leftPos + 6, topPos + 5, 77, 12),
                new ScreenRectangleWidgetParam(0, Math.min(searchBoxY, 18*rows + 40), 20, 20),
                new ScreenRectangleWidgetParam(leftPos + 85, topPos + 5, 12, 12)
        );
    }

    @Override
    public int rows() {
        return pageParam.rows();
    }

    @Override
    public int columns() {
        return pageParam.columns();
    }

    @Override
    public int leftPos() {
        return leftPos;
    }

    @Override
    public int topPos() {
        return topPos;
    }

    @Override
    public TextureMode textureMode() {
        return textureMode;
    }

    @Override
    public List<PageType> pages() {
        return displayPages;
    }

    @Override
    public int pageTabCount() {
        return pageSwitchBarParam.maxBars();
    }

    @Override
    public IRectangleParam pageParam() {
        return pageRectangleParam;
    }

    @Override
    public IRectangleParam pageTabDec() {
        return pageSwitchBarParam.buttonDec();
    }

    @Override
    public IRectangleParam pageTabInc() {
        return pageSwitchBarParam.buttonInc();
    }

    @Override
    public IRectangleParam sortBox() {
        return sortBoxParam;
    }

    @Override
    public IRectangleParam configButton() {
        return configButtonParam;
    }

    @Override
    public IRectangleParam reverseSortButton() {
        return reverseSortButtonParam;
    }

    @Override
    public IRectangleParam searchBox() {
        return searchBoxParam;
    }
}
