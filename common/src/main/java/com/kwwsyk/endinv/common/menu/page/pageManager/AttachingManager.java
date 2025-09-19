package com.kwwsyk.endinv.common.menu.page.pageManager;

import com.kwwsyk.endinv.common.EndlessInventory;
import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.common.SourceInventory;
import com.kwwsyk.endinv.common.menu.page.PageType;
import com.kwwsyk.endinv.common.menu.page.PageTypeRegistry;
import com.kwwsyk.endinv.common.network.payloads.PageData;
import com.kwwsyk.endinv.common.network.payloads.toClient.EndInvMetadata;
import com.kwwsyk.endinv.common.util.SortType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class AttachingManager implements PageMetaDataManager{

    private final AbstractContainerMenu menu;
    private final EndlessInventory endinv;
    private final ServerPlayer player;
    //client for page switch
    private final List<String> PageIdList;
    private final List<PageType> pageTypeList;
    private String DisplayingPageId;
    private int displayingPageIndex;
    private final PageQuickMoveHandler quickMoveHandler;
    public SortType sortType;
    public String searching;
    private int rows;
    private int columns;
    private boolean reverseSort;

    public AttachingManager(AbstractContainerMenu menu, EndlessInventory endinv, ServerPlayer player){
        this.menu = menu;
        this.endinv = endinv;
        this.player = player;
        pageTypeList = PageTypeRegistry.getDisplayPages();
        PageIdList = pageTypeList.stream().map(tp->tp.registerName).toList();
        this.rows = 9;
        this.columns = 9;
        this.sortType = SortType.DEFAULT;
        this.searching = "";
        this.reverseSort = false;
        if (!PageIdList.isEmpty()) {
            this.displayingPageIndex = 0;
            this.DisplayingPageId = PageIdList.get(0);
        } else {
            this.displayingPageIndex = 0;
            this.DisplayingPageId = PageType.DEFAULT_KEY;
        }
        this.quickMoveHandler = new PageQuickMoveHandler(this);
    }

    public void applyPageData(PageData data){
        this.rows = data.rows();
        this.columns = data.columns();
        this.sortType = data.sortType();
        this.searching = data.search();
        this.reverseSort = data.reverseSort();
        this.switchPageWithId(data.pageRegKey());
    }


    @Override
    public AbstractContainerMenu getMenu() {
        return menu;
    }

    @Override
    public SourceInventory getSourceInventory() {
        return endinv;
    }

    @Override
    public void switchPageWithIndex(int index) {
        displayingPageIndex = index;
        DisplayingPageId = PageIdList.get(index);
    }

    @Override
    public int rows() {
        return rows;
    }

    @Override
    public int columns() {
        return columns;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public int getItemSize() {
        return endinv.getItemSize();
    }

    @Override
    public int getMaxStackSize() {
        return endinv.getMaxItemStackSize();
    }

    @Override
    public boolean enableInfinity() {
        return endinv.isInfinityMode();
    }

    @Override
    public ItemStack quickMoveFromPage(ItemStack stack) {
        return this.quickMoveHandler.quickMoveFromPage(stack);
    }

    @Override
    public SortType sortType() {
        return sortType;
    }

    @Override
    public void setSortType(SortType sortType) {
        this.sortType = sortType;
    }

    @Override
    public boolean isSortReversed() {
        return this.reverseSort;
    }

    @Override
    public void switchSortReversed() {
        this.reverseSort = !this.reverseSort;
    }

    @Override
    public void setSortReversed(boolean reversed) {
        this.reverseSort = reversed;
    }

    @Override
    public String searching() {
        return searching;
    }

    @Override
    public void setSearching(String searching) {
        this.searching= searching;
    }

    @Override
    public void sendEndInvData() {
        ModInfo.getPacketDistributor().sendToPlayer(player,EndInvMetadata.getWith(endinv));
    }

    @Override
    public void switchPageWithId(String id) {
        DisplayingPageId = id;
        for(int i=0; i<PageIdList.size();++i){
            if(PageIdList.get(i).equals(id)){
                displayingPageIndex = i;
            }
        }
    }

    @Override
    public String getDisplayingPageId() {
        return DisplayingPageId;
    }

    @Override
    public PageType getDisplayingPageType() {
        return pageTypeList.get(displayingPageIndex);
    }
}


