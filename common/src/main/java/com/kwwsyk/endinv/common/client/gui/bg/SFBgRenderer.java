package com.kwwsyk.endinv.common.client.gui.bg;

import com.kwwsyk.endinv.common.client.gui.ScreenFramework;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface SFBgRenderer {

    ScreenFramework getScreenFrameWork();

    void renderBg(@NotNull GuiGraphicsExtractor GuiGraphicsExtractor, float partialTicks, int mouseX, int mouseY);

    IRectangleParam pageSwitchBarParam();

    default Optional<PageBgRender> getDefaultPageBgRenderer(){
        return Optional.empty();
    }

    @FunctionalInterface
    interface PageBgRender {
        void renderBg(@NotNull GuiGraphicsExtractor GuiGraphicsExtractor, float partialTicks, int mouseX, int mouseY);
    }
}

