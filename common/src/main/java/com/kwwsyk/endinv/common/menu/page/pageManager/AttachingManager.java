package com.kwwsyk.endinv.common.menu.page.pageManager;

import com.kwwsyk.endinv.common.EndlessInventory;
import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.common.SourceInventory;
import com.kwwsyk.endinv.common.client.gui.page.DisplayPage;
import com.kwwsyk.endinv.common.menu.page.PageType;
import com.kwwsyk.endinv.common.menu.page.PageTypeRegistry;
import com.kwwsyk.endinv.common.network.payloads.PageData;
import com.kwwsyk.endinv.common.network.payloads.SyncedConfig;
import com.kwwsyk.endinv.common.network.payloads.toClient.EndInvMetadata;
import com.kwwsyk.endinv.common.network.payloads.toServer.ItemPageContext;
import com.kwwsyk.endinv.common.util.SortType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import static com.kwwsyk.endinv.common.ModRegistries.NbtAttachments.getSyncedConfig;

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
        SyncedConfig config = getSyncedConfig().getWith(player);
        init(config.pageData());
        this.quickMoveHandler = new PageQuickMoveHandler(this);
    }
    private void init(int rows, int columns, SortType sortType, String searching, String type){
        this.rows = rows;
        this.columns = columns;
        this.sortType = sortType;
        this.searching = searching;
        this.switchPageWithId(type);
    }
    private void init(PageData data){
        init(data.rows(),data.columns(),data.sortType(),data.search(),data.pageRegKey());
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
    @Deprecated
    public List<DisplayPage> getPages() {
        throw new IllegalStateException("DisplayPage is already client only.");
    }

    @Override@Deprecated
    public DisplayPage getDisplayingPage() {
        throw new IllegalStateException("DisplayPage is already client only.");
    }

    @Override
    public void switchPageWithIndex(int index) {
        displayingPageIndex = index;
        DisplayingPageId = PageIdList.get(index);
    }

    @Override
    public int getRowCount() {
        return rows;
    }

    @Override
    public int getColumnCount() {
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
    public int getDisplayingPageIndex() {
        return displayingPageIndex;
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

    @Override @Deprecated
    public List<DisplayPage> buildPages() {
        throw new IllegalStateException("Display page is only available on client.");
    }

    @Override
    public String getDisplayingPageId() {
        return DisplayingPageId;
    }

    @Override
    public PageType getDisplayingPageType() {
        return pageTypeList.get(displayingPageIndex);
    }

    @Override @Deprecated
    public ItemPageContext getInPageContext() {
        throw new IllegalStateException("Display page is only available on client.");
    }
}
