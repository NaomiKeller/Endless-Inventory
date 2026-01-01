package com.kwwsyk.endinv.common.client.gui.page;

import com.kwwsyk.endinv.common.client.gui.ScreenFramework;
import com.kwwsyk.endinv.common.menu.page.PageType;
import com.kwwsyk.endinv.common.network.payloads.toServer.StarItemPayload;
import com.kwwsyk.endinv.common.util.ItemStackLike;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.kwwsyk.endinv.common.ModInfo.getPacketDistributor;

public class StarredItemPage extends ItemPage{

    public ResourceLocation icon = ResourceLocation.fromNamespaceAndPath("minecraft", "book");
    private int[] countArray;

    public StarredItemPage(PageType type, ScreenFramework screenFramework) {
        super(type, screenFramework);
    }

    public void starItem(ItemStack stack, boolean isAdding){
        if(stack.isEmpty()) return;
        getPacketDistributor().sendToServer(new StarItemPayload(stack,isAdding));
        requestRemoteContents();
    }

    @Override
    public ResourceLocation getIcon(){
        return icon;
    }

    @Override
    protected void setVisibleRange(int startIndex, int length){
        this.startIndex = startIndex;
        this.length = Math.min(length, framework.rows()* framework.columns());
        this.items = NonNullList.withSize(length, ItemStack.EMPTY);
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

    public void initializeContents(@NotNull List<ItemStack> stacks){
        if(holdOn){
            inQueueStacks = stacks;
            return;
        }
        for(int i=0; i<items.size(); ++i){
            if(i<stacks.size() && stacks.get(i)!=null){
                items.set(i,stacks.get(i).copyWithCount(1));
                countArray[i]=stacks.get(i).getCount();
            }else {
                items.set(i,ItemStack.EMPTY);
            }
        }
    }

    public void initializeAsMap(@NotNull List<ItemStackLike> stacks){
        for(int i=0; i<items.size(); ++i){
            if(i<stacks.size() && stacks.get(i)!=null){
                items.set(i,stacks.get(i).toKey());
                countArray[i]=stacks.get(i).count();
            }else {
                items.set(i,ItemStack.EMPTY);
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
            int itemX = leftPos + MARGIN_SIDE_WIDTH + columnIndex*18,itemY = topPos + MARGIN_TOP_HEIGHT + rowIndex*18+1;
            ItemStack stack = items.get(i);
            int count = countArray[i];
            guiGraphics.renderItem(stack,itemX,itemY,columnIndex+rowIndex*180);
            if(!isHiddenBySortBox(rowIndex,columnIndex))
                guiGraphics.renderItemDecorations(Minecraft.getInstance().font, stack,itemX,itemY, getDisplayAmount(stack.copyWithCount(count)));
            columnIndex++;
            if(columnIndex>= framework.columns()){
                columnIndex=0;
                rowIndex++;
            }
        }
        guiGraphics.pose().popPose();
    }

    @Override
    public void handleStarItem(double XOffset, double YOffset) {
        int slot = getSlotByMouseOffset(XOffset,YOffset);
        if(slot>=0&&slot<items.size()) {
            ItemStack clicked = items.get(slot);
            starItem(clicked,false);
        }
    }

    public void release(){
        if(holdOn){
            holdOn = false;
            if(inQueueStacks==null) return;
            initializeContents(inQueueStacks);
        }
    }
}
