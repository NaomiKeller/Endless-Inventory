package com.kwwsyk.endinv.common.network.payloads.toServer;

import com.kwwsyk.endinv.common.EndlessInventory;
import com.kwwsyk.endinv.common.ServerLevelEndInv;
import com.kwwsyk.endinv.common.menu.EndlessInventoryMenu;
import com.kwwsyk.endinv.common.menu.page.pageManager.AttachingMonitor;
import com.kwwsyk.endinv.common.menu.page.pageManager.PageMetaDataManager;
import com.kwwsyk.endinv.common.network.payloads.ModPacketContext;
import com.kwwsyk.endinv.common.network.payloads.ModPacketPayload;
import com.kwwsyk.endinv.common.network.payloads.PageData;
import com.kwwsyk.endinv.common.network.payloads.toClient.EndInvContent;
import com.kwwsyk.endinv.common.network.payloads.toClient.EndInvMetadata;
import com.kwwsyk.endinv.common.network.payloads.toClient.SetItemDisplayContentPayload;
import com.kwwsyk.endinv.common.options.ContentTransferMode;
import com.kwwsyk.endinv.common.util.SortType;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

import static com.kwwsyk.endinv.common.ModInfo.getPacketDistributor;
import static com.kwwsyk.endinv.common.ModInfo.getServerConfig;

/**
 * In page context used on Page operations.
 *
 * @param startIndex
 * @param length
 * @param pageData
 */
public record ItemPageContext(int startIndex, int length, PageData pageData) implements ModPacketPayload {

    //ContentTransferMode.ALL used to resend the *entire* inventory on every one of these packets -
    //fired on every scroll notch, keystroke, and click - regardless of whether anything had
    //actually changed since the last send. On a large "All Items" page that's a full-inventory
    //copy, serialization, and network round trip dozens of times a second while scrolling. Keyed
    //on the manager instance (a fresh one is created every time the screen is opened - see
    //AttachingMonitor's construction in OpenEndInvPayload and EndlessInventoryMenu) rather than
    //the player or inventory, so a reconnect or menu reopen always starts with a cache miss and
    //gets a real resync instead of an incorrectly-skipped one; WeakHashMap lets stale entries be
    //collected once their manager is no longer referenced elsewhere, avoiding a manual cleanup.
    private static final Map<PageMetaDataManager, Long> lastSyncedModTime =
            Collections.synchronizedMap(new WeakHashMap<>());

    public static void encode(ItemPageContext context, FriendlyByteBuf o) {
        o.writeInt(context.startIndex);
        o.writeInt(context.length);
        PageData.encode(o, context.pageData);
    }

    public static ItemPageContext decode(FriendlyByteBuf o) {
        return new ItemPageContext(o.readInt(), o.readInt(), PageData.decode(o));
    }

    public SortType sortType() {
        return pageData.sortType();
    }

    public String search() {
        return pageData.search();
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ItemPageContext context)) return false;
        return length == context.length && startIndex == context.startIndex && Objects.equals(pageData, context.pageData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startIndex, length, pageData);
    }

    @Override
    public String id() {
        return "page_context";
    }

    public void handle(ModPacketContext iPayloadContext) {
        ServerPlayer serverPlayer = (ServerPlayer) iPayloadContext.player();
        assert serverPlayer!=null;
        var optional = ServerLevelEndInv.checkAndGetManagerForPlayer(serverPlayer);
        optional.ifPresent(manager -> {

            if (!Objects.equals(manager.getPageData(), this.pageData)) {
                SortType sortType = pageData.sortType();
                boolean reverseSort = pageData.reverseSort();
                String search = pageData.search();

                if (manager instanceof AttachingMonitor attachingMonitor) {
                    attachingMonitor.applyPageData(pageData);
                } else if (manager instanceof EndlessInventoryMenu menuManager) {
                    menuManager.applyPageData(pageData);
                } else {
                    manager.setSortType(sortType);
                    manager.setSortReversed(reverseSort);
                    manager.setSearching(search);
                    manager.switchPageWithId(pageData().pageRegKey());
                }
            }

            //== ServerEndInv#sendEndInvContent
            EndlessInventory endInv = (EndlessInventory) manager.getSourceInventory();
            if (getServerConfig().transferMode().get() == ContentTransferMode.PART) {
                List<ItemStack> view = endInv.getSortedAndFilteredItemView(startIndex, length,
                        manager.sortType(), manager.isSortReversed(),
                        manager.getDisplayingPageType().itemClassify, manager.searching());

                NonNullList<ItemStack> stacks = NonNullList.withSize(length, ItemStack.EMPTY);
                for (int i = 0; i < view.size(); ++i) {
                    stacks.set(i, view.get(i));
                }
                getPacketDistributor().sendToPlayer(serverPlayer, EndInvMetadata.getWith(endInv));
                getPacketDistributor().sendToPlayer(serverPlayer, new SetItemDisplayContentPayload(stacks));
            } else if (getServerConfig().transferMode().get() == ContentTransferMode.ALL) {
                long currentModTime = endInv.getLastModTime();
                Long lastSynced = lastSyncedModTime.get(manager);
                if (lastSynced == null || lastSynced != currentModTime) {
                    getPacketDistributor().sendToPlayer(serverPlayer, new EndInvContent(endInv.snapshotItemMap()));
                    lastSyncedModTime.put(manager, currentModTime);
                }
            }
        });
    }
}


