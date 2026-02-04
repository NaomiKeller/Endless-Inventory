package com.kwwsyk.endinv.common.client.gui.page;

import com.kwwsyk.endinv.common.client.CachedSrcInv;
import com.kwwsyk.endinv.common.client.gui.page.manager.PageManager;
import com.kwwsyk.endinv.common.client.option.CachedConfig;
import com.kwwsyk.endinv.common.menu.page.PageType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**Page that displays EndInv's items (directly {@link CachedSrcInv})
 *
 */
public class ItemDisplay extends ItemPage{

    public ItemDisplay(PageType pageType, PageManager metaDataManager) {
        super(pageType,metaDataManager);
    }

    public void refreshItems(){
        requestRemoteContents();
        readCachedItems();
    }

    /**
     * Read items data from {@link CachedSrcInv} when transfer mode is {@code ALL} and rebuild the view.
     */
    public void readCachedItems(){
        List<ItemPointer> view = CachedSrcInv.INSTANCE.getItemView(startIndex,length,
                CachedConfig.sortType(), CachedConfig.reverseSort(),
                getClassify(), CachedConfig.searching());
        buildContentsWith(view);
    }

    public void requestRemoteContents(){
        sendChangesToServer();
    }

    /**
     * Build Displayed view with a ItemStack list.
     * @param stacks itemstack list to fill the view
     */
    public void buildContentsWith(@NotNull List<ItemPointer> stacks){
        if(holdOn){
            inQueueStacks = stacks;
            return;
        }
        for(int i=0; i<this.length; ++i){
            if(i<stacks.size() && stacks.get(i) != null) {
                this.items.set(i, stacks.get(i));
            }else {
                this.items.set(i,ItemPointer.EMPTY);
            }
        }
    }

    @Override
    public boolean hasSearchbox() {
        return true;
    }

    @Override
    public boolean hasSortTypeSwitchBar() {
        return true;
    }

    public void release(){
        if(holdOn){
            holdOn = false;
            if(inQueueStacks==null) return;
            buildContentsWith(inQueueStacks);
        }
    }
}
