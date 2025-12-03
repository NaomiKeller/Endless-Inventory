package com.kwwsyk.endinv.common.client.gui.widget;

import com.kwwsyk.endinv.common.AbstractModInitializer;
import com.kwwsyk.endinv.common.client.gui.ScreenFramework;
import com.kwwsyk.endinv.common.client.option.ClientConfigs;
import com.kwwsyk.endinv.common.client.option.PageSwitchBarConfig;
import com.kwwsyk.endinv.common.client.option.TextureMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class PageSwitchBar extends AbstractWidget {

    //public static final ResourceLocation TABS_RESOURCE = ResourceLocation.withDefaultNamespace("textures/gui/advancements/tabs.png");
    private static final ResourceLocation TAB_LEFT_MIDDLE_SPRITE = ResourceLocation.withDefaultNamespace("advancements/tab_left_middle");
    private static final ResourceLocation TAB_LEFT_TOP_SELECTED = ResourceLocation.withDefaultNamespace("advancements/tab_left_top_selected");
    private static final ResourceLocation TAB_LEFT_MIDDLE_SELECTED = ResourceLocation.withDefaultNamespace("advancements/tab_left_middle_selected");
    private static final ResourceLocation TAB_LEFT_BOTTOM_SELECTED = ResourceLocation.withDefaultNamespace("advancements/tab_left_bottom_selected");

    private static final ResourceLocation TAB_UNSELECTED = AbstractModInitializer.withModLocation("textures/gui/tab_left_middle.png");
    private static final ResourceLocation TAB_TOP = AbstractModInitializer.withModLocation("textures/gui/tab_left_top_selected.png");
    private static final ResourceLocation TAB_MIDDLE = AbstractModInitializer.withModLocation("textures/gui/tab_left_middle_selected.png");
    private static final ResourceLocation TAB_BOTTOM = AbstractModInitializer.withModLocation("textures/gui/tab_left_bottom_selected.png");

    private static final int PAGE_FRAME_COLOR = 0x80A0A0A0;
    private static final int PAGE_BG_COLOR = 0x30373737;

    private static ResourceLocation getTabsTexture(TabType type){
        return ClientConfigs.ATTACHED_MENU_CONFIG.TextureMode.get() == TextureMode.DEDICATED_LOCATION ? type.dedicatedLocation : type.vanillaTexture;
    }

    private enum TabType{
        UNSELECTED(TAB_LEFT_MIDDLE_SPRITE, TAB_UNSELECTED),
        TOP(TAB_LEFT_TOP_SELECTED, TAB_TOP),
        MIDDLE(TAB_LEFT_MIDDLE_SELECTED, TAB_MIDDLE),
        BOTTOM(TAB_LEFT_BOTTOM_SELECTED, TAB_BOTTOM);

        final ResourceLocation vanillaTexture;
        final ResourceLocation dedicatedLocation;

        TabType(ResourceLocation vanillaTexture, ResourceLocation dedicatedLocation){
            this.vanillaTexture = vanillaTexture;
            this.dedicatedLocation = dedicatedLocation;
        }
    }

    ScreenFramework framework;
    int maxBars;
    TextureMode textureMode;

    boolean direction_isVertical;
    int tabWidth,tabHeight;

    public PageSwitchBar(ScreenFramework screenFramework, PageSwitchBarConfig.Param param, int adjustedMaxBars, TextureMode textureMode) {
        super(
                param.tabParam().x(),
                param.tabParam().y(),
                param.direction_isVertical() ? param.tabParam().width() : param.tabParam().height() * adjustedMaxBars,
                param.direction_isVertical() ? param.tabParam().height() * adjustedMaxBars : param.tabParam().width(),
                Component.empty()
        );
        framework = screenFramework;
        maxBars = adjustedMaxBars;
        this.textureMode = textureMode;

        direction_isVertical = param.direction_isVertical();
        tabWidth = param.tabParam().width();
        tabHeight = param.tabParam().height();
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        double XOffset = mouseX - getX();
        double YOffset = mouseY - getY();
        if(XOffset < 0 || XOffset > getWidth() || YOffset < 0 || YOffset > getHeight()) return;
        int index;
        if(direction_isVertical){
            index = (int)YOffset/tabHeight;
        }else index = (int)XOffset/tabWidth;
        framework.pageSwitched(index);
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int tabX = getX();
        int tabY = getY();
        int selectedPageIndex = framework.getDisplayingPageIndex();
        if(textureMode == TextureMode.TRANSPARENT){
            for (int i = ScreenFramework.firstPageIndex; i < ScreenFramework.firstPageIndex + framework.pageBarCount; ++i) {
                if (i == selectedPageIndex) {
                    guiGraphics.fill(tabX,tabY,tabX+tabWidth,tabY+tabHeight,PAGE_FRAME_COLOR);
                } else {
                    guiGraphics.fill(tabX+4,tabY,tabX+tabWidth,tabY+tabHeight,PAGE_BG_COLOR);
                }
                if(direction_isVertical) tabY+=tabHeight; else tabX+=tabWidth;
            }
        }else {
            for (int i = ScreenFramework.firstPageIndex; i < ScreenFramework.firstPageIndex + framework.pageBarCount; ++i) {
                if (i == selectedPageIndex) {
                    if (i == 0) {
                        guiGraphics.blitSprite(RenderType::guiTextured, getTabsTexture(TabType.TOP),tabX,tabY,tabWidth,tabHeight);
                    } else if (i == ScreenFramework.firstPageIndex + framework.pageBarCount-1) {
                        guiGraphics.blitSprite(RenderType::guiTextured, getTabsTexture(TabType.BOTTOM),tabX,tabY,tabWidth,tabHeight);
                    } else
                        guiGraphics.blitSprite(RenderType::guiTextured, getTabsTexture(TabType.MIDDLE),tabX,tabY,tabWidth,tabHeight);
                } else {
                    guiGraphics.blitSprite(RenderType::guiTextured, getTabsTexture(TabType.UNSELECTED),tabX+4,tabY,tabWidth,tabHeight);
                }
                if(direction_isVertical) tabY+=tabHeight; else tabX+=tabWidth;
            }
        }
        tabX = getX();
        tabY = getY();
        for (int i = ScreenFramework.firstPageIndex; i < ScreenFramework.firstPageIndex + framework.pageBarCount; ++i) {
            framework.getPages().get(i).renderPageIcon(guiGraphics, tabX + 15, tabY + 5, partialTick);
            if (mouseX > tabX && mouseX < tabX + tabWidth && mouseY > tabY && mouseY < tabY + tabHeight) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, 550.0f);
                guiGraphics.renderTooltip(Minecraft.getInstance().font, framework.getPages().get(i).name, mouseX, mouseY);
                guiGraphics.pose().popPose();
            }
            if(direction_isVertical) tabY+=tabHeight; else tabX+=tabWidth;
        }
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }
}
