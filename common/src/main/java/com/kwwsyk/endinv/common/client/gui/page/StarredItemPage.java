package com.kwwsyk.endinv.common.client.gui.page;

import com.kwwsyk.endinv.common.client.gui.page.manager.PageManager;
import com.kwwsyk.endinv.common.menu.page.PageType;
import com.kwwsyk.endinv.common.network.payloads.toServer.StarItemPayload;
import com.kwwsyk.endinv.common.util.ItemKey;
import com.kwwsyk.endinv.common.util.ItemStackLike;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.kwwsyk.endinv.common.ModInfo.getPacketDistributor;

public class StarredItemPage extends ItemPage{

    private int[] countArray;

    public StarredItemPage(PageType type, PageManager metaDataManager) {
        super(type, metaDataManager);
    }

    public void starItem(ItemStack stack, boolean isAdding){
        if(stack.isEmpty()) return;
        getPacketDistributor().sendToServer(new StarItemPayload(stack,isAdding));
        requestRemoteContents();
    }

    @Override
    public void initializeContents(int startIndex, int length){
        this.startIndex = startIndex;
        this.length = Math.min(length, meta.rows()* meta.columns());
        this.items = NonNullList.withSize(length, ItemPointer.EMPTY);
        this.countArray = new int[length];
        this.refreshItems();
    }

    /**
     * The <em>refresh</em> method of ItemPage, this method shall keep the startIndex and length and fill {@link #items}
     * with such and srcInv.
     */
    @Override
    public void refreshItems() {
        requestRemoteContents();
    }

    public void initializeContents(@NotNull List<ItemPointer> stacks){
        if(holdOn){
            inQueueStacks = stacks;
            return;
        }
        for(int i=0; i<items.size(); ++i){
            if(i<stacks.size() && stacks.get(i)!=null){
                items.set(i,stacks.get(i));
                countArray[i]=stacks.get(i).get().getCount();
            }else {
                items.set(i,ItemPointer.EMPTY);
            }
        }
    }

    public void initializeAsMap(@NotNull List<ItemStackLike> stacks){
        for(int i=0; i<items.size(); ++i) {
            if(i<stacks.size() && stacks.get(i)!=null){ ItemStackLike itemStackLike = stacks.get(i);
                items.set(i,new ItemPointer(new ItemKey(itemStackLike.item(), itemStackLike.tag()))); 
                countArray[i]= itemStackLike.count(); 
            }else { 
                items.set(i,ItemPointer.EMPTY); countArray[i]=0; 
            } 
        } 
    }

    public void requestRemoteContents(){
        getPacketDistributor().sendToServer(new StarItemPayload(ItemStack.EMPTY,false));
    }

    @Override
    public boolean hasSearchbox() {
        return true;
    }

    @Override
    public boolean hasSortTypeSwitchBar() {
        return false;
    }

    @Override
    public void renderPage(GuiGraphics guiGraphics){
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
        int rowIndex = 0;
        int columnIndex = 0;
        for(int i=0; i<length; ++i){
            ItemPointer pointer = (ItemPointer) items.get(i);
            ItemKey key = pointer.key();
            if(key != null && !key.isEmpty()){
                int x = leftPos+columnIndex*18;
                int y = topPos+rowIndex*18+1;
                ItemStack live = pointer.get();
                int count = countArray[i];
                //a starred item currently at 0 count is still absent from the live inventory snapshot,
                //so pointer.get() comes back empty; fall back to the key itself so the bookmark's icon
                //keeps showing instead of vanishing once the player runs out of the item.
                ItemStack stack = live.isEmpty() ? key.toStack(1) : live;
                guiGraphics.renderItem(stack,x,y,columnIndex+rowIndex*180);
                if(!isHiddenBySortBox(rowIndex,columnIndex))
                    guiGraphics.renderItemDecorations(Minecraft.getInstance().font, stack,x,y,
                            live.isEmpty() ? ChatFormatting.RED+"0" : getDisplayAmount(stack.copyWithCount(count)));
            }
            columnIndex++;
            if(columnIndex>= meta.columns()){
                columnIndex=0;
                rowIndex++;
            }
        }
        guiGraphics.pose().popPose();
    }

    @Override
    public void handleStarItem(double XOffset, double YOffset) {
        starItem(resolveStack(getSlotByMouseOffset(XOffset,YOffset)),false);
    }

    @Override
    public ItemStack getItemByMouseOffset(double XOffset, double YOffset){
        return resolveStack(getSlotByMouseOffset(XOffset,YOffset));
    }

    /**
     * Resolve the ItemStack to show/act on for a slot, falling back to the starred key itself
     * (count 1) when the live inventory snapshot has none of the item, so bookmarks for items
     * the player is currently out of remain hoverable/un-starrable instead of looking empty.
     */
    private ItemStack resolveStack(int slot){
        if(slot<0 || slot>=items.size()) return ItemStack.EMPTY;
        ItemPointer pointer = (ItemPointer) items.get(slot);
        ItemKey key = pointer.key();
        if(key==null || key.isEmpty()) return ItemStack.EMPTY;
        ItemStack live = pointer.get();
        return live.isEmpty() ? key.toStack(1) : live;
    }

    public void release(){
        if(holdOn){
            holdOn = false;
            if(inQueueStacks==null) return;
            initializeContents(inQueueStacks);
        }
    }
}
