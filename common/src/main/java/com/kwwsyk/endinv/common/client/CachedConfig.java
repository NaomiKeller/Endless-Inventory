package com.kwwsyk.endinv.common.client;

import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.common.ModRegistries;
import com.kwwsyk.endinv.common.client.option.IClientConfig;
import com.kwwsyk.endinv.common.menu.page.PageType;
import com.kwwsyk.endinv.common.network.payloads.PageData;
import com.kwwsyk.endinv.common.network.payloads.SyncedConfig;
import com.kwwsyk.endinv.common.util.SortType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;

public final class CachedConfig {

    private static String pageRegKey = PageType.DEFAULT_KEY;
    private static int rows = 6;
    private static int columns = 9;
    private static SortType sortType = SortType.DEFAULT;
    private static boolean reverseSort = false;
    private static String searching = "";

    private static boolean attaching = true;

    private static SyncedConfig cachedFlags = SyncedConfig.DEFAULT;
    private static PageData cachedLayout = PageData.DEFAULT;
    private static boolean layoutInitialized;

    private CachedConfig() {
    }

    public static void readAndSyncClientConfigToServer(boolean refreshLayout) {
        if (refreshLayout || !layoutInitialized) {
            cachedLayout = resolveLayout(null, true);
        }
        synchronizeFlags();
    }

    public static void synchronizeFlags() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        IClientConfig config = ClientModInfo.getClientConfig();
        boolean attaching = config.attaching().get();
        SyncedConfig desired = new SyncedConfig(attaching, cachedFlags.autoPicking());
        if (!desired.equals(cachedFlags)) {
            cachedFlags = desired;
            ModRegistries.NbtAttachments.getSyncedConfig().setTo(player, desired);
            ModInfo.getPacketDistributor().sendToServer(desired);
        }
    }

    public static void acceptServerFlags(SyncedConfig flags) {
        cachedFlags = flags;
    }

    public static PageData resolveLayout(@Nullable AbstractContainerScreen<?> screen, boolean ofMenu) {
        IClientConfig config = ClientModInfo.getClientConfig();

        int rows = config.rows().get();
        if (rows <= 0) {
            rows = config.calculateDefaultRowCount(ofMenu);
        }
        if (rows <= 0) {
            rows = cachedLayout.rows() > 0 ? cachedLayout.rows() : PageData.DEFAULT.rows();
        }

        int columns = config.columns().get();
        if (columns <= 0) {
            columns = PageData.DEFAULT.columns();
        }
        if (screen != null && config.autoSuitColumn().get()) {
            int fitted = config.calculateSuitInColumnCount(screen);
            columns = Math.min(columns, fitted);
        }
        if (columns <= 0) {
            columns = cachedLayout.columns() > 0 ? cachedLayout.columns() : PageData.DEFAULT.columns();
        }

        cachedLayout = new PageData(rows,columns);
        layoutInitialized = true;
        return cachedLayout;
    }

    public static PageData currentLayout() {
        if (!layoutInitialized) {
            cachedLayout = resolveLayout(null, true);
        }
        return cachedLayout;
    }

    public static void updateLayout(PageData layout) {
        cachedLayout = layout;
        layoutInitialized = true;
    }
}
