package com.kwwsyk.endinv.common.client.gui.page;

import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.common.client.gui.ScreenFramework;
import com.kwwsyk.endinv.common.client.gui.page.manager.ResourcePointer;
import com.kwwsyk.endinv.common.menu.page.PageType;
import com.kwwsyk.endinv.common.menu.page.pageManager.PageQuickMoveHandler;
import com.kwwsyk.endinv.common.network.payloads.PageData;
import com.kwwsyk.endinv.common.network.payloads.toServer.*;
import com.kwwsyk.endinv.common.util.ItemKey;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;

import java.util.List;

import static com.kwwsyk.endinv.common.ModInfo.getPacketDistributor;

/**Page that holds ItemStack list. It's the main type of pages.
 *
 */
public abstract class ItemPage extends GridPage {

    private static final Logger LOGGER = LogUtils.getLogger();

    protected NonNullList<ResourcePointer<ItemStack>> items;

    protected List<ItemPointer> inQueueStacks = null;

    public ItemPage(PageType pageType, ScreenFramework framework) {
        super(pageType, framework);
    }

    /**
     * Adjust the number of slots maintained by the item page so it matches the latest menu layout.
     */
    @Override
    public void resize(int rows) {
        int targetRows = Math.max(1, rows);
        this.length = targetRows * framework.columns();
        setVisibleRange(this.startIndex, this.length);
    }

    /**Configure the visible window and reload items.
     * <p>
     * Updates the page's starting index and slot count, ensures the internal
     * {@link #items} list matches the requested size, clears transient state via {@link #release()},
     * then calls {@link #refreshItems()} to populate the visible entries.
     * </p>
     * @param startIndex the source-inventory index of the first visible item
     * @param length the number of slots to display (typically rows * columns)
     */
    @Override
    protected void setVisibleRange(int startIndex, int length) {
        this.startIndex = startIndex;
        this.length = Math.min(length, framework.rows()* framework.columns());
        // ensure internal invariants
        if(this.items==null || this.items.size()!=this.length) this.items = NonNullList.withSize(this.length, ItemPointer.EMPTY);
        if(length != this.items.size()){
            this.items = NonNullList.withSize(length,ItemPointer.EMPTY);
        }
        release();
        this.refreshItems();
    }

    /**The <em>refresh</em> method of ItemPage, this method shall keep the startIndex and length and fill {@link #items}
     * with such and srcInv.<br>
     * This may invoke {@link #requestRemoteContents()} or other send packet methods.
     * So be caution use this method especially on receiving request-remote-callback packets. Like {@link com.kwwsyk.endinv.common.network.payloads.toClient.EndInvContent}
     */
    public abstract void refreshItems();

    public abstract void requestRemoteContents();

    /**The change usually means pageMetaData changes and called by framework in sort,search,... changes.<br>
     * For ItemPage and ItemDisplay: also used to request contents as sending {@link ItemPageContext} has such side effect.
     */
    public void sendChangesToServer() {
        var layout = new PageData(this.id, framework.rows(), framework.columns(), framework.sortType(), framework.isSortReversed(), framework.searching());
        getPacketDistributor().sendToServer(new ItemPageContext(getStartIndex(), length, layout));//send this packet will receive a content packet as callback
    }

    public boolean isEmpty() {
        return items.stream().allMatch(p -> p == null || p.get().isEmpty());
    }

    @Override
    public boolean canScroll() {
        return getStartIndex() >0 ||(getStartIndex() +length <= srcInv.getItemSize());
    }

    /**Get mouse hovered or clicked item by mouse offset.
     * @param XOffset mouseX-pageX
     * @param YOffset mouseY-pageY
     * @return hovered or clicked item
     */
    @Override
    public ItemStack getItemByMouseOffset(double XOffset, double YOffset){
        int slot = getSlotByMouseOffset(XOffset,YOffset);
        if(slot>=0 && slot<items.size()) {
            ResourcePointer<ItemStack> p = items.get(slot);
            return p == null ? ItemStack.EMPTY : p.get();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void handleStarItem(double XOffset, double YOffset) {
        ItemStack clicked = getItemByMouseOffset(XOffset, YOffset);
        if(clicked.isEmpty()) return;
        getPacketDistributor().sendToServer(new StarItemPayload(clicked,true));
    }

    public void renderPage(GuiGraphics guiGraphics){
        int rowIndex = 0;
        int columnIndex = 0;
        for(ResourcePointer<ItemStack> pointer : items){
            ItemStack stack = pointer.get();
            int itemX = leftPos + MARGIN_SIDE_WIDTH + columnIndex*18,itemY = topPos + MARGIN_TOP_HEIGHT + rowIndex*18+1;
            if(stack.isEmpty() && !stack.is(Items.AIR)) renderEmpty(guiGraphics,itemX,itemY,stack);
            guiGraphics.renderItem(stack,itemX,itemY,columnIndex+rowIndex*180);
            if(!isHiddenBySortBox(rowIndex,columnIndex))
                guiGraphics.renderItemDecorations(Minecraft.getInstance().font, stack,itemX,itemY, getDisplayAmount(stack));
            columnIndex++;
            if(columnIndex>= framework.columns()){
                columnIndex=0;
                rowIndex++;
            }
        }
    }

    @Override
    public void pageClicked(double XOffset, double YOffset, int button, ClickType clickType) {
        int slot = getSlotByMouseOffset(XOffset,YOffset);
        if(slot>=0 && slot<items.size()) {
            ItemStack clicked = getItemByMouseOffset(XOffset, YOffset).copy();
            switch (clickType){
                case PICKUP -> handlePickup(clicked, button);
                case QUICK_MOVE -> handleQuickMove(clicked);
                case SWAP -> handleSwap(clicked, slot);
                case THROW -> handleThrow(clicked);
                case CLONE -> handleClone(clicked);
                case PICKUP_ALL -> handlePickupAll(clicked);
            }
            LOGGER.info("EI:sending:ItemClickPayload: player={} clickType={} button={} stack={}"
                    , framework.getPlayer(),clickType,button, clicked);
            if(clickType == ClickType.PICKUP_ALL && Minecraft.getInstance().hasShiftDown()) return;
            ModInfo.getPacketDistributor().sendToServer(new ItemClickPayload(
                    ItemKey.asKey(clicked),
                    button,clickType));
            this.refreshItems();
        }
    }

    public ItemStack takeItem(ItemStack itemStack){
        return takeItem(itemStack,itemStack.getMaxStackSize());
    }

    public ItemStack takeItem(ItemStack itemStack,int count){
        setChanged();
        return this.srcInv.takeItem(itemStack,count);
    }

    public ItemStack takeItem(int index, int count){
        ResourcePointer<ItemStack> p = this.items.get(index);
        ItemStack itemStack = p == null ? ItemStack.EMPTY : p.get();
        setChanged();
        return srcInv.takeItem(itemStack,count);
    }

    public ItemStack addItem(ItemStack itemStack){
        setChanged();
        return srcInv.addItem(itemStack.copy());
    }

    public boolean isFull(ItemStack itemStack){
        return itemStack.getCount() >= framework.getMaxStackSize();
    }

    public boolean isInfinite(ItemStack itemStack){
        return  isFull(itemStack) && framework.enableInfinity();
    }

    protected void handleQuickMove(ItemStack clicked){
        ItemStack taken = takeItem(clicked);
        ItemStack remain = framework.quickMoveFromPage(taken);
        addItem(remain);
        setChanged();
    }
    @Override
    public ItemStack tryInsertItem(ItemStack stack) {
        var remain = addItem(stack);
        initializeContents();
        return remain;
    }
    @Override
    public ItemStack tryExtractItem(ItemStack stack,int count){
        return takeItem(stack,count);
    }

    protected void handlePickup(ItemStack clicked, int keyCode){
        ItemStack carried = framework.getMenu().getCarried();
        if(!carried.isEmpty()){
            ItemStack remain = addItem(carried.copy());
            if(ModInfo.isClientLoaded() && framework.getMenu() instanceof CreativeModeInventoryScreen.ItemPickerMenu){
                getPacketDistributor().sendToServer(new CreativeItemModPayload(carried.copy(),true));
            }
            framework.getMenu().setCarried(remain);
            setChanged();
        }else{
            int count = Math.min(clicked.getCount(),clicked.getMaxStackSize());
            int takenCount = keyCode==0 ? count : (count + 1) / 2;
            framework.getMenu().setCarried(takeItem(clicked,takenCount));
            if(!framework.getMenu().getCarried().isEmpty()) setChanged();
        }
    }

    protected void handleSwap(ItemStack clicked, int inventorySlotId){
        Player player = framework.getPlayer();
        Inventory inventory = player.getInventory();
        ItemStack inventoryItem = inventory.getItem(inventorySlotId);
        boolean a = !inventoryItem.isEmpty();
        boolean b = !clicked.isEmpty();
        if( a && !b ){
            ItemStack remain = addItem(inventoryItem);
            inventory.setItem(inventorySlotId, remain);
        }
        if( !a && b ){
            ItemStack swapping = takeItem(clicked); //take most
            inventory.setItem(inventorySlotId,swapping);
        }
        if( a && b ){
            ItemStack remain = addItem(inventoryItem);
            if(remain.isEmpty()) {
                ItemStack swapping = takeItem(clicked); //take most
                inventory.setItem(inventorySlotId, swapping);
            }else {
                inventory.setItem(inventorySlotId,remain);
            }
        }
        setChanged();
    }
    protected void handleThrow(ItemStack clicked){
        Player player = framework.getPlayer();
        ItemStack thrown = takeItem(clicked);
        player.drop(thrown,true);
        setChanged();
    }
    protected void handlePickupAll(ItemStack clicked){
        // Shift + Double Click: bulk quick-move from Endless Inventory page into the open container
        if (Minecraft.getInstance().hasShiftDown()) {
            ModInfo.getPacketDistributor().sendToServer(new BulkQuickMoveFromPagePayload(ItemKey.asKey(clicked.copyWithCount(1))));
            int iterations = 0;
            var mover = new PageQuickMoveHandler(framework);
            while (iterations++ < 32768) {
                ItemStack taken = takeItem(clicked.copyWithCount(1));
                if (taken.isEmpty()) break;
                ItemStack remain = mover.quickMoveFromPage(taken);
                if (!remain.isEmpty()) {
                    // Could not insert fully; put the remainder back and stop.
                    addItem(remain);
                    break;
                }
            }
            setChanged();
            release();
            // Ensure the view updates immediately so no temporary empty slot remains
            this.refreshItems();
            return;
        }
        Player player = framework.getPlayer();
        ItemStack carried = framework.getMenu().getCarried();
        int startIndex = framework.getMenu().slots.size() - 1; //changed: reversed button==0 condition
        for(int index = startIndex; index>=0 ; --index){
            Slot scanning = framework.getMenu().slots.get(index);
            if(!(scanning.container instanceof Inventory)) break;
            ItemStack scanningItem =scanning.getItem();
            if (ItemStack.isSameItemSameComponents(carried, scanningItem)) {
                ItemStack taken = scanning.safeTake(scanningItem.getCount(), scanningItem.getCount(), player);
                ItemStack remain = addItem(taken);
                if(!remain.isEmpty()) scanning.set(remain);
                setChanged();
            }
        }
    }
    protected void handleClone(ItemStack clicked){
        Player player = framework.getPlayer();
        if(player.getAbilities().instabuild && framework.getMenu().getCarried().isEmpty()){
            framework.getMenu().setCarried(clicked.copyWithCount(clicked.getMaxStackSize()));
        }
    }

    public static class ItemPointer implements ResourcePointer<ItemStack> {

        public static final ItemPointer EMPTY = new ItemPointer(null){
            public ItemStack get(){
                return ItemStack.EMPTY;
            }

            @Override
            public boolean isEmpty() {
                return true;
            }
        };

        private final com.kwwsyk.endinv.common.SourceInventory srcInv;
        private final ItemKey key;

        public ItemPointer(ItemKey key){
            this.srcInv = com.kwwsyk.endinv.common.client.CachedSrcInv.INSTANCE;
            this.key = key;
        }

        @Override
        public ItemStack get() {
            if(key == null) return ItemStack.EMPTY;
            var state = srcInv.getItemMap().get(key);
            if(state == null) return ItemStack.EMPTY;
            return key.toStack(state.count());
        }

        public ItemKey key(){
            return key;
        }

        public boolean isEmpty(){
            return key == null;
        }
    }
}


