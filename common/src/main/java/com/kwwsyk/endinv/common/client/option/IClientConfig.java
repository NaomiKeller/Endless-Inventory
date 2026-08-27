package com.kwwsyk.endinv.common.client.option;

import com.kwwsyk.endinv.common.options.IConfigValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import java.util.Set;

public interface IClientConfig {

    IConfigValue<Boolean> attaching();

    /** Rows/columns used by the standalone EndInv screen (opened with its own keybind, shown centered). */
    IConfigValue<Integer> menuRows();

    IConfigValue<Integer> menuColumns();

    /** Rows/columns used when EndInv is attached beside another screen (e.g. survival inventory, a chest). */
    IConfigValue<Integer> attachedRows();

    IConfigValue<Integer> attachedColumns();

    IConfigValue<Boolean> autoSuitColumn();

    IConfigValue<TextureMode> textureMode();

    IConfigValue<Boolean> screenDebugging();

    IConfigValue<Integer> maxPageBarCount();

    Set<String> hidingPageIds();

    void setPageHiding(String id, boolean hiding);

    default void save(){}

    default boolean isPageHidden(String id){
        return hidingPageIds().contains(id);
    }

    default int calculateDefaultRowCount(boolean ofMenu){
        Minecraft mc = Minecraft.getInstance();
        int height = mc.getWindow().getGuiScaledHeight();
        //ofMenu reserves an extra row's worth of height versus the attached layout: 4 rows for the
        //vanilla player-inventory block, plus 1 more for the bottom search/config bar appended
        //below it (EndlessInventoryScreen.BOTTOM_BAR_HEIGHT + its gap, ~24px).
        return Math.max(Math.floorDiv(height-60,18)-(ofMenu?5:0),0);
    }

    default int calculateSuitInColumnCount(AbstractContainerScreen<?> screen){
        int leftPos = (screen.width - 176)/2;
        int width = leftPos - 20 - 6 -6;
        return Math.max(0,Math.floorDiv(width,18));
    }
}
