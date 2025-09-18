package com.kwwsyk.endinv.common.client.gui.bg;

import com.kwwsyk.endinv.common.client.gui.ScreenFramework;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface SFBgRenderer {

    ScreenFramework getScreenFrameWork();

    void renderBg(@NotNull GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY);

    ScreenRectangleWidgetParam pageSwitchBarParam();

    default Optional<PageBgRender> getDefaultPageBgRenderer(){
        return Optional.empty();
    }

    @FunctionalInterface
    interface PageBgRender {
        void renderBg(@NotNull GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY);
    }
}
