package com.kwwsyk.endinv.common.client.gui.page.manager;

import com.kwwsyk.endinv.common.SourceInventory;
import com.kwwsyk.endinv.common.client.gui.page.DisplayPage;
import com.kwwsyk.endinv.common.menu.page.PageType;
import com.kwwsyk.endinv.common.menu.page.PageTypeRegistry;
import com.kwwsyk.endinv.common.menu.page.pageManager.PageMetaDataManager;
import com.kwwsyk.endinv.common.network.payloads.toServer.ItemPageContext;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Objects;

public class ClientPageManager implements PageManager {

    private final PageMetaDataManager delegate;
    private final List<DisplayPage> pages;
    private DisplayPage displayingPage;

    public ClientPageManager(PageMetaDataManager delegate){
        this.delegate = delegate;
        this.pages = PageTypeRegistry.getDisplayPages().stream().map(type -> type.buildPage(this)).toList();
        if(!pages.isEmpty()){
            this.displayingPage = pages.get(0);
        }
    }

    @Override
    public List<DisplayPage> getPages() {
        return pages;
    }

    @Override
    public DisplayPage getDisplayingPage() {
        return displayingPage;
    }

    @Override
    public AbstractContainerMenu getMenu() {
        return delegate.getMenu();
    }

    @Override
    public SourceInventory getSourceInventory() {
        return delegate.getSourceInventory();
    }

    @Override
    public Player getPlayer() { return delegate.getPlayer(); }

    @Override
    public void switchPageWithIndex(int index) {
        delegate.switchPageWithIndex(index);
        if(index>=0 && index<pages.size()){
            this.displayingPage = pages.get(index);
            this.displayingPage.refreshContents();
        }
    }

    @Override
    public int rows() {
        return delegate.rows();
    }

    @Override
    public int columns() {
        return delegate.columns();
    }

    @Override
    public int getItemSize() {
        return delegate.getItemSize();
    }

    @Override
    public int getMaxStackSize() {
        return delegate.getMaxStackSize();
    }

    @Override
    public boolean enableInfinity() {
        return delegate.enableInfinity();
    }

    @Override
    public ItemStack quickMoveFromPage(ItemStack stack) {
        return delegate.quickMoveFromPage(stack);
    }

    @Override
    public com.kwwsyk.endinv.common.util.SortType sortType() {
        return delegate.sortType();
    }

    @Override
    public void setSortType(com.kwwsyk.endinv.common.util.SortType sortType) {
        delegate.setSortType(sortType);
    }

    @Override
    public boolean isSortReversed() {
        return delegate.isSortReversed();
    }

    @Override
    public void setSortReversed(boolean reversed) {
        delegate.setSortReversed(reversed);
    }

    @Override
    public String searching() {
        return delegate.searching();
    }

    @Override
    public void setSearching(String searching) {
        delegate.setSearching(searching);
    }

    @Override
    public void sendEndInvData() {
        delegate.sendEndInvData();
    }

    @Override
    public String getDisplayingPageId() {
        return displayingPage != null ? displayingPage.id : delegate.getDisplayingPageId();
    }

    @Override
    public void switchPageWithId(String id) {
        for(int i=0;i<pages.size();++i){
            if(Objects.equals(pages.get(i).id,id)){
                switchPageWithIndex(i);
                return;
            }
        }
        delegate.switchPageWithId(id);
    }

    @Override
    public PageType getDisplayingPageType() {
        return displayingPage != null ? displayingPage.getPageType() : delegate.getDisplayingPageType();
    }

    @Override
    public com.kwwsyk.endinv.common.network.payloads.toServer.ItemPageContext getInPageContext() {
        DisplayPage page = getDisplayingPage();
        return new ItemPageContext(
                page instanceof com.kwwsyk.endinv.common.client.gui.page.ItemPage itemPage ? itemPage.getStartIndex() : 0,
                rows()* columns(),
                getPageData()
        );
    }

    @Override
    public void scrollTo(float pos) {
        if(displayingPage!=null) displayingPage.scrollTo(pos);
    }

    @Override
    public int getDisplayingPageIndex(){
        for(int i=0;i<pages.size();++i) if(pages.get(i)==displayingPage) return i;
        return -1;
    }

}