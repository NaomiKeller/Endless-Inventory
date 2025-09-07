package com.kwwsyk.endinv.common.menu.page.pageManager;

import com.kwwsyk.endinv.common.SourceInventory;
import com.kwwsyk.endinv.common.client.gui.page.manager.PageManager;
import com.kwwsyk.endinv.common.menu.page.PageType;
import com.kwwsyk.endinv.common.network.payloads.PageData;
import com.kwwsyk.endinv.common.util.SortType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * if {@code Dist.isClient}, pageMetaDataManagers shall be able to cast to {@link PageManager}
 */
public interface PageMetaDataManager {

    AbstractContainerMenu getMenu();

    SourceInventory getSourceInventory();

    Player getPlayer();

    void switchPageWithIndex(int index);

    int rows();

    int columns();

    int getItemSize();

    int getMaxStackSize();

    boolean enableInfinity();

    ItemStack quickMoveFromPage(ItemStack stack);

    SortType sortType();

    void setSortType(SortType sortType);

    boolean isSortReversed();

    default void switchSortReversed(){
        setSortReversed(!isSortReversed());
    }

    void setSortReversed(boolean reversed);

    String searching();

    void setSearching(String searching);

    void sendEndInvData();

    String getDisplayingPageId();

    void switchPageWithId(String id);

    PageType getDisplayingPageType();

    default void slotQuickMoved(Slot clicked) {
        ItemStack itemStack = clicked.getItem();
        ItemStack remain = getSourceInventory().addItem(itemStack);
        clicked.setByPlayer(remain);
        clicked.onTake(getPlayer(), itemStack);
    }

    default PageData getPageData(){
        return new PageData(getDisplayingPageId(), rows(), columns(),sortType(),isSortReversed(),searching());
    }

}
