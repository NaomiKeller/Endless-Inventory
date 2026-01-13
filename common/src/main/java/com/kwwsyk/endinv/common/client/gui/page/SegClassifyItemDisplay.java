package com.kwwsyk.endinv.common.client.gui.page;

import com.kwwsyk.endinv.common.client.CachedSrcInv;
import com.kwwsyk.endinv.common.client.gui.ScreenFramework;
import com.kwwsyk.endinv.common.menu.page.PageType;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Item display page that groups entries into segments defined by sub classifications.
 * Each segment is rendered on its own set of rows. Items that do not match any subclassify
 * predicate can optionally form an additional trailing segment.
 */
public class SegClassifyItemDisplay extends ItemDisplay {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final List<Predicate<ItemStack>> subClassifies;
    private final boolean includeRemainItems;
    private final boolean keepClassifiedItemInNextSeg;

    private final List<ItemStack> segmentedView = new ArrayList<>();
    /**
     * Slot indexes inside {@link #segmentedView} that represent the first column of each segment.
     * This list preserves insertion order so we can translate them into row numbers for the
     * currently displayed slice during {@link #updateDisplayedSlice()}.
     */
    private final List<Integer> segmentStartSlots = new ArrayList<>();
    /** Row indexes (relative to the currently rendered page) that should render a separator line. */
    private final List<Integer> pageSeparatorRows = new ArrayList<>();

    public SegClassifyItemDisplay(PageType pageType,
                                  ScreenFramework screenFramework,
                                  List<Predicate<ItemStack>> subClassifies,
                                  boolean includeRemainItems,
                                  boolean keepClassifiedItemInNextSeg) {
        super(pageType, screenFramework);
        this.subClassifies = subClassifies == null ? List.of() : List.copyOf(subClassifies);
        this.includeRemainItems = includeRemainItems;
        this.keepClassifiedItemInNextSeg = keepClassifiedItemInNextSeg;
    }

    public void readCachedItems() {
        List<ItemStack> source = CachedSrcInv.INSTANCE.getSortedAndFilteredItemView(
                0,
                Integer.MAX_VALUE,
                framework.sortType(),
                framework.isSortReversed(),
                getClassify(),
                framework.searching());
        buildContentsWith(source);
    }

    /**
     * Build Displayed view with a ItemStack list.
     *
     * @param stacks itemstack list to fill the view
     */
    @Override
    public void buildContentsWith(@NotNull List<ItemStack> stacks) {
        if(holdOn){
            inQueueStacks = stacks;
            return;
        }
        rebuildSegments(stacks);
    }

    private void buildContentDirectly(List<ItemStack> stacks){
        for(int i=0; i<this.length; ++i){
            if(i<stacks.size() && stacks.get(i) != null) {
                this.items.set(i, stacks.get(i).copy());
            }else {
                this.items.set(i,ItemStack.EMPTY);
            }
        }
    }

    @Override
    public void renderPage(GuiGraphics guiGraphics) {
        int rowIndex = 0;
        int columnIndex = 0;
        int columns = framework.columns();
        for (ItemStack item : items) {
            int itemX = leftPos + MARGIN_SIDE_WIDTH + columnIndex*18,itemY = topPos + MARGIN_TOP_HEIGHT + rowIndex*18+1;
            if (columnIndex == 0 && pageSeparatorRows.contains(rowIndex)) {
                guiGraphics.hLine(itemX, leftPos + MARGIN_SIDE_WIDTH + columns * 18 - 2, itemY, 0xFF5A5A5A);
            }
            if (item.isEmpty() && !item.is(Items.AIR)) {
                renderEmpty(guiGraphics, itemX, itemY, item);
            }
            guiGraphics.renderItem(item, itemX, itemY, columnIndex + rowIndex * 180);
            if (!isHiddenBySortBox(rowIndex, columnIndex)) {
                guiGraphics.renderItemDecorations(Minecraft.getInstance().font, item, itemX, itemY, getDisplayAmount(item));
            }
            columnIndex++;
            if (columnIndex >= columns) {
                columnIndex = 0;
                rowIndex++;
            }
        }
    }

    @Override
    public ItemStack takeItem(ItemStack itemStack, int count) {
        setChanged();
        ItemStack result = this.srcInv.takeItem(itemStack, count);
        readCachedItems();
        return result;
    }

    @Override
    public ItemStack takeItem(int index, int count) {
        int viewIndex = getStartIndex() + index;
        if (viewIndex < 0 || viewIndex >= segmentedView.size()) {
            return ItemStack.EMPTY;
        }
        ItemStack itemStack = segmentedView.get(viewIndex);
        if (itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        setChanged();
        ItemStack result = srcInv.takeItem(itemStack, count);
        readCachedItems();
        return result;
    }

    @Override
    public ItemStack addItem(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        setChanged();
        ItemStack remain = srcInv.addItem(itemStack.copy());
        readCachedItems();
        return remain;
    }

    private void rebuildSegments(List<ItemStack> source) {
        segmentedView.clear();
        segmentStartSlots.clear();
        pageSeparatorRows.clear();
        List<ItemStack> filtered = new ArrayList<>();
        for (ItemStack stack : source) {
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            filtered.add(stack.copy());
        }
        if (filtered.isEmpty()) {
            buildContentDirectly(List.of());
            return;
        }

        boolean[] consumed = new boolean[filtered.size()];
        boolean[] matchedAtLeastOnce = new boolean[filtered.size()];
        for (Predicate<ItemStack> classifier : subClassifies) {
            if (classifier == null) {
                continue;
            }
            int start = segmentedView.size();
            List<ItemStack> segment = new ArrayList<>();
            for (int i = 0; i < filtered.size(); ++i) {
                if (!keepClassifiedItemInNextSeg && consumed[i]) {
                    continue;
                }
                ItemStack stack = filtered.get(i);
                if (classifier.test(stack)) {
                    segment.add(stack.copy());
                    matchedAtLeastOnce[i] = true;
                    if (!keepClassifiedItemInNextSeg) {
                        consumed[i] = true;
                    }
                }
            }
            if (!segment.isEmpty()) {
                segmentStartSlots.add(start);
                appendSegment(segmentedView, segment);
            }
        }

        if (includeRemainItems) {
            List<ItemStack> remain = new ArrayList<>();
            for (int i = 0; i < filtered.size(); ++i) {
                boolean matched = matchedAtLeastOnce[i];
                boolean consumedFlag = keepClassifiedItemInNextSeg ? matched : consumed[i];
                if (!consumedFlag) {
                    remain.add(filtered.get(i).copy());
                }
            }
            if (!remain.isEmpty()) {
                segmentStartSlots.add(segmentedView.size());
                appendSegment(segmentedView, remain);
            }
        }

        updateDisplayedSlice();
    }

    private void appendSegment(List<ItemStack> target, List<ItemStack> segment) {
        if (segment.isEmpty()) {
            return;
        }
        target.addAll(segment);
        int columns = framework.columns();
        if (columns <= 0) {
            return;
        }
        int remainder = segment.size() % columns;
        if (remainder == 0) {
            return;
        }
        int filler = columns - remainder;
        for (int i = 0; i < filler; ++i) {
            target.add(ItemStack.EMPTY);
        }
    }

    private void updateDisplayedSlice() {
        int columns = Math.max(1, framework.columns());
        int fromIndex = Math.min(getStartIndex(), segmentedView.size());
        int toIndex = Math.min(fromIndex + length, segmentedView.size());
        List<ItemStack> slice = segmentedView.subList(fromIndex, toIndex);
        buildContentDirectly(slice);
        pageSeparatorRows.clear();
        if (!segmentStartSlots.isEmpty()) {
            int firstRow = fromIndex / columns;
            int rowEndExclusive = Mth.positiveCeilDiv(Math.max(toIndex, fromIndex), columns);
            for (Integer start : segmentStartSlots) {
                if (start == null) {
                    continue;
                }
                int row = start / columns;
                if (row <= firstRow) {
                    continue;
                }
                if (row < rowEndExclusive) {
                    int relative = row - firstRow;
                    if (!pageSeparatorRows.contains(relative)) {
                        pageSeparatorRows.add(relative);
                    }
                }
            }
            if (!pageSeparatorRows.isEmpty()) {
                pageSeparatorRows.sort(Integer::compareTo);
            }
        }
    }
}
