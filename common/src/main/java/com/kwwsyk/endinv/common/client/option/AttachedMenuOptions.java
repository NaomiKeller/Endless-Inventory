package com.kwwsyk.endinv.common.client.option;

import com.kwwsyk.endinv.common.client.ClientModInfo;
import com.kwwsyk.endinv.common.client.gui.EndlessInventoryScreen;
import com.kwwsyk.endinv.common.client.gui.bg.IRectangleParam;
import com.kwwsyk.endinv.common.client.gui.bg.ScreenRectangleWidgetParam;
import com.kwwsyk.endinv.common.menu.page.PageType;
import com.kwwsyk.endinv.common.menu.page.PageTypeRegistry;
import com.kwwsyk.endinv.common.options.config.EntryPresentable;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AbstractContainerMenu;

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

    static final IRectangleParam PAGE_RECTANGLE = new ScreenRectangleWidgetParam(0,0,9*18 + 8 + 8, 17 + 15*18 + 12);
    static final IRectangleParam SEARCH_BOX = new ScreenRectangleWidgetParam(1, 17 + 15*18 + 12, 8 + 9*18 + 8, 15);
    static final IRectangleParam SORT_BOX = new ScreenRectangleWidgetParam(6, 5, 77, 12);
    static final IRectangleParam REVERSE_SORT_BUTTON = new ScreenRectangleWidgetParam(6 + 77 + 2, 5, 12, 12);
    static final IRectangleParam CONFIG_BUTTON = new ScreenRectangleWidgetParam(-20,-20,20,20);

    public static final AttachedMenuOptions DEFAULT = new AttachedMenuOptions(
            BasicLayout.LEFT,
            PageBasicLayoutConfig.Param.DEFAULT,
            PageTypeRegistry.getDisplayPages(),
            PageSwitchBarConfig.Param.DEFAULT,
            20,20,
            TextureMode.FROM_RESOURCE,
            PAGE_RECTANGLE,//todo next we include the margin to page's area, it's the newer param.
            SEARCH_BOX,
            SORT_BOX,
            CONFIG_BUTTON,//in the left-down corner of the screen
            REVERSE_SORT_BUTTON
    );

    public final BasicLayout basicLayout;

    final PageBasicLayoutConfig.Param pageParam;
    final List<PageType> displayPages;

    final int leftPos;
    final int topPos;

    final TextureMode textureMode;
    final IRectangleParam pageRectangleParam;
    //only active in FULL_CUSTOMIZE mode
    public final PageSwitchBarConfig.Param pageSwitchBarParam;
    final IRectangleParam searchBoxParam;
    final IRectangleParam sortBoxParam;
    final IRectangleParam configButtonParam;
    final IRectangleParam reverseSortButtonParam;

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
            int topPos,
            PageBasicLayoutConfig.Param pageParam,
            List<PageType> displayPages,
            TextureMode textureMode,
            IRectangleParam pageRect
    ){
        return new AttachedMenuOptions(
                BasicLayout.LEFT, pageParam, displayPages, PageSwitchBarConfig.Param.DEFAULT, 20, topPos, textureMode, pageRect,
                SEARCH_BOX, SORT_BOX, CONFIG_BUTTON, REVERSE_SORT_BUTTON
        );
    }

    /**
     * @param adjustedLeftPos if not adjusted, -1
     */
    public static AttachedMenuOptions ofRight(
            int adjustedLeftPos, int topPos,
            PageBasicLayoutConfig.Param pageParam,
            List<PageType> displayPages,
            TextureMode textureMode,
            IRectangleParam pageRect
    ){
        return new AttachedMenuOptions(
                BasicLayout.RIGHT, pageParam, displayPages, PageSwitchBarConfig.Param.DEFAULT, adjustedLeftPos, topPos, textureMode, pageRect,
                SEARCH_BOX, SORT_BOX, CONFIG_BUTTON, REVERSE_SORT_BUTTON
        );
    }

    public AttachedMenuOptions adjust(AbstractContainerScreen<?> screen){
        if(screen instanceof EndlessInventoryScreen){
            throw new IllegalArgumentException("EIS should not use AS's options.");
        }
        AbstractContainerMenu menu = screen.getMenu();
        int menuLeft = ClientModInfo.containerScreenHelper.getGuiLeft(screen);
        int menuTop = ClientModInfo.containerScreenHelper.getGuiTop(screen);
        int menuRight = menuLeft + ClientModInfo.containerScreenHelper.getGuiXSize(screen);
        switch (basicLayout){
            case LEFT : {
                int rows = calculateRows(screen.height - topPos - 15);
                int columns = calculateColumns(menuLeft - leftPos - /*Reversed widget space*/30);
                return new AttachedMenuOptions(
                        BasicLayout.LEFT,
                        new PageBasicLayoutConfig.Param(rows, columns, false,false),
                        displayPages, pageSwitchBarParam, leftPos, topPos, textureMode,
                        new ScreenRectangleWidgetParam(
                                0, 0,
                                textureMode==TextureMode.TRANSPARENT ? pageRectangleParam.width() : columns * 18 + 8 + 8,
                                textureMode==TextureMode.TRANSPARENT ? pageRectangleParam.height() : rows * 18 + 17 + 12
                        ),
                        adjustSearchBox(screen, textureMode == TextureMode.TRANSPARENT ? pageRectangleParam.height() : rows * 18 +17+12),
                        sortBoxParam,
                        adjustConfigButton(textureMode == TextureMode.TRANSPARENT ? pageRectangleParam.height() : rows * 18 +17+12),
                        reverseSortButtonParam
                );
            }
            case MERGE_JEI : {//JEI integrate coming soon
            }
            case RIGHT : {
                int rows = calculateRows(screen.height - topPos - 15);
                int columns = calculateColumns(screen.width - menuRight);
                int sfW = (pageSwitchBarParam.direction_isVertical() ? pageSwitchBarParam.tabParam().width() : 0)
                        + (textureMode == TextureMode.TRANSPARENT ? pageRectangleParam.width() : pageParam.columns() * 18 + 8 + 8);
                return new AttachedMenuOptions(
                        BasicLayout.RIGHT,
                        new PageBasicLayoutConfig.Param(rows, columns, false, false),
                        displayPages,
                        pageSwitchBarParam,
                        screen.width - sfW, topPos,
                        textureMode,
                        new ScreenRectangleWidgetParam(
                                0, 0,
                                textureMode==TextureMode.TRANSPARENT ? pageRectangleParam.width() : columns * 18 + 8 + 8,
                                textureMode==TextureMode.TRANSPARENT ? pageRectangleParam.height() : rows * 18 + 17 + 12
                        ),
                        adjustSearchBox(screen, textureMode == TextureMode.TRANSPARENT ? pageRectangleParam.height() : rows * 18 +17+12),
                        sortBoxParam,
                        adjustConfigButton(textureMode == TextureMode.TRANSPARENT ? pageRectangleParam.height() : rows * 18 +17+12),
                        reverseSortButtonParam
                );
            }
            case FULL_CUSTOMIZE : {
                return this;
            }
            default : throw new IllegalStateException("Unexpected value: " + basicLayout);
        }
    }

    private int calculateRows(int givenPageY){
        boolean treatAsAuto = pageParam.autoRows() || pageParam.rows() <= 0; // rows==0 means auto
        if(textureMode == TextureMode.TRANSPARENT){
            int pageH = pageRectangleParam.height() > 0 ? pageRectangleParam.height() : givenPageY;
            if(treatAsAuto){
                pageH = Math.min(givenPageY, pageRectangleParam.height());
            }
            int rows = Math.floorDiv(pageH, 18);// marginH = pageH % 18 / 2
            return Math.max(1, rows);
        }
        // marginH = 17
        int rows = treatAsAuto ? Math.floorDiv(givenPageY - 17 - 12, 18) : pageParam.rows();
        // When not auto, ensure rows does not exceed configured maximum; when auto, just clamp minimum
        if (!treatAsAuto) {
            rows = Math.min(rows, pageParam.rows());
        }
        return Math.max(1, rows);
    }

    private int calculateColumns(int givenPageX){
        boolean treatAsAuto = pageParam.autoColumns() || pageParam.columns() <= 0; // columns==0 means auto
        if(textureMode == TextureMode.TRANSPARENT){
            int pageW = pageRectangleParam.width() > 0 ? pageRectangleParam.width() : givenPageX;
            if(treatAsAuto){
                pageW = Math.min(givenPageX, pageRectangleParam.width());
            }
            int columns = Math.floorDiv(pageW, 18);// marginW = pageW % 18 / 2
            return Math.max(1, columns);
        }
        //marginW = 8
        int columns = treatAsAuto ? Math.floorDiv(givenPageX - 8 - 8, 18) : pageParam.columns();
        if (!treatAsAuto) {
            columns = Math.min(columns, pageParam.columns());
        }
        return Math.max(1, columns);
    }

    private IRectangleParam adjustSearchBox(AbstractContainerScreen<?> screen,int givenPageY){
        return new ScreenRectangleWidgetParam(searchBoxParam.x(), givenPageY, searchBoxParam.width(), Mth.clamp(searchBoxParam.height(), 10, screen.height - topPos - givenPageY));
    }

    private IRectangleParam adjustConfigButton(int givenPageY){
        return new ScreenRectangleWidgetParam(configButtonParam.x(), givenPageY, configButtonParam.width(), configButtonParam.height());
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
    public IRectangleParam pageParamA() {
        return pageRectangleParam.applyOffset(leftPos, topPos);
    }

    @Override
    public IRectangleParam pageTabA() {
        return pageSwitchBarParam.tabParam().applyOffset(leftPos, topPos);
    }

    @Override
    public IRectangleParam pageTabDecA() {
        return pageSwitchBarParam.buttonDec().applyOffset(leftPos, topPos);
    }

    @Override
    public IRectangleParam pageTabIncA() {
        return pageSwitchBarParam.buttonInc().applyOffset(leftPos, topPos);
    }

    @Override
    public IRectangleParam sortBoxA() {
        return sortBoxParam.applyOffset(leftPos, topPos);
    }

    @Override
    public IRectangleParam configButtonA() {
        return configButtonParam.applyOffset(leftPos, topPos);
    }

    @Override
    public IRectangleParam reverseSortButtonA() {
        return reverseSortButtonParam.applyOffset(leftPos, topPos);
    }

    @Override
    public IRectangleParam searchBoxA() {
        return searchBoxParam.applyOffset(leftPos, topPos);
    }

    @Override
    public AttachedMenuOptions clone(){
        try{
            super.clone();
        }catch (CloneNotSupportedException e){
            throw new RuntimeException("An exception that should not happen: "+e);
        }
        return new AttachedMenuOptions(basicLayout, pageParam, displayPages, pageSwitchBarParam, leftPos, topPos, textureMode, pageRectangleParam, searchBoxParam, sortBoxParam, configButtonParam, reverseSortButtonParam);
    }
}
