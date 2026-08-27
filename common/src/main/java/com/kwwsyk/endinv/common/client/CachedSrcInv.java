package com.kwwsyk.endinv.common.client;

import com.google.common.collect.Lists;
import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.common.SourceInventory;
import com.kwwsyk.endinv.common.client.gui.page.ItemPage;
import com.kwwsyk.endinv.common.network.payloads.toClient.EndInvMetadata;
import com.kwwsyk.endinv.common.options.ContentTransferMode;
import com.kwwsyk.endinv.common.util.ItemKey;
import com.kwwsyk.endinv.common.util.ItemState;
import com.kwwsyk.endinv.common.util.SearchUtil;
import com.kwwsyk.endinv.common.util.SortType;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.Util;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**Client only {@link SourceInventory}
 * Served as data cache of EndlessInventory.
 */
public class CachedSrcInv extends SourceInventory {

    public static final CachedSrcInv INSTANCE = new CachedSrcInv();
    private int itemSize;
    //controls whether itemSize data is valid when transfermode==all.
    private boolean validSize;

    private CachedSrcInv(){
        super(ModInfo.DEFAULT_UUID);
    }

    public void initializeContents(Map<ItemKey, ItemState> itemMap){
        overwriteItems(new Object2ObjectLinkedOpenHashMap<>(itemMap));
        this.itemSize = getItemSize();//here assert transfermode==all //parallelable
        this.validSize = true;
    }

    public List<ItemPage.ItemPointer> getItemView(int startIndex,
                                                  int length,
                                                  SortType sortType,
                                                  boolean reverse,
                                                  @Nullable Predicate<ItemStack> classify,
                                                  String search){
        var filtered = getSortedKeyReference(sortType)
                .filter(key -> {
                    ItemStack stack = key.toStack(itemMap.get(key).count());
                    return (classify == null || classify.test(stack)) && SearchUtil.matchesSearch(stack, search);
                })
                .map(ItemPage.ItemPointer::new)
                .toList();
        //reverse the whole list before windowing, not after: reversing only the sliced window left
        //a short tail once startIndex was past the halfway point of the list (fewer than `length`
        //items remained on that side), which showed up as trailing blank slots once scrolled.
        List<ItemPage.ItemPointer> ordered = reverse ? Lists.reverse(filtered) : filtered;
        //startIndex is computed from the row-count estimate in DisplayPage#calculateRowCount(),
        //which is based on the whole inventory's item count rather than this page's actual
        //filtered/classified list. On a page with fewer items than the total (Gear, Stone,
        //Consumables, ...), that estimate can overshoot this list's real size, so clamp the start
        //to it too - not just the end - or subList throws when startIndex > ordered.size().
        int size = ordered.size();
        int from = Math.min(Math.max(startIndex, 0), size);
        int to = Math.min(Math.max(startIndex + length, from), size);
        return ordered.subList(from, to);
    }

    /**
     * @return Size of Endless Inventory.
     */
    @Override
    public int getItemSize() {
        if(ModInfo.getServerConfig().transferMode().get()== ContentTransferMode.PART) return itemSize;
        if(this.validSize) return itemSize;
        return super.getItemSize();
    }

    @Override
    public ItemStack takeItem(ItemStack stack, int count){
        if(stack.isEmpty()) return ItemStack.EMPTY;
        ItemKey key = ItemKey.asKey(stack);
        ItemState state = itemMap.get(key);
        if (state == null) return ItemStack.EMPTY;
        //if infinity
        if(state.count() >= maxStackSize && infinityMode){
            setChanged();
            return stack.copyWithCount(count);
        }

        int taken = Math.min(count, state.count());
        ItemStack result = stack.copyWithCount(taken);
        if (taken == state.count()) {
            itemMap.remove(key);
            updateLastModTime();
        } else {
            itemMap.put(key, new ItemState(state.count() - taken, updateLastModTime()));
        }
        setChanged();
        return result;

    }

    @Override
    public ItemStack addItem(ItemStack itemStack){
        if(itemStack.isEmpty()) return ItemStack.EMPTY;
        ItemKey key = ItemKey.asKey(itemStack);
        ItemState state = itemMap.get(key);
        int count = itemStack.getCount();
        int original = 0;

        if (state != null) {
            original = state.count();
        }
        int increased;
        if(original < maxStackSize){
            increased = original+count;
            if(increased <= maxStackSize){
                itemMap.put(key, new ItemState(increased, updateLastModTime()));
                setChanged();
                return ItemStack.EMPTY;
            }else {
                itemMap.put(key, new ItemState(maxStackSize, updateLastModTime()));
                setChanged();
                return itemStack.copyWithCount(increased-maxStackSize);
            }
        }else if(infinityMode){
            itemMap.put(key, new ItemState(original, updateLastModTime()));
            setChanged();
            return ItemStack.EMPTY;
        }else {
            return itemStack.copy();
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        validSize = false;
    }

    public long updateLastModTime(){
        return Util.getMillis();
    }

    public void syncMetadata(EndInvMetadata endInvMetadata) {
        this.itemSize = endInvMetadata.itemSize();
        this.maxStackSize = endInvMetadata.maxStackSize();
        this.infinityMode = endInvMetadata.infinityMode();
        this.accessibility =endInvMetadata.config().accessibility();
        this.owner = endInvMetadata.config().owner();
        this.white_list = endInvMetadata.config().white_list();
    }
}
