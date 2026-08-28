package com.kwwsyk.endinv.common.client.gui.bg;

import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.common.client.ClientModInfo;
import com.kwwsyk.endinv.common.client.gui.ScreenFramework;
import com.kwwsyk.endinv.common.client.option.TextureMode;
import com.kwwsyk.endinv.common.menu.EndlessInventoryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@SuppressWarnings("removal")
public abstract class FromResource extends SFBgRendererImpl {


    public static final ResourceLocation CONTAINER_TEXTURE_RESOURCE = new ResourceLocation("minecraft","textures/gui/container/generic_54.png");
    public static final ResourceLocation TABS_RESOURCE = new ResourceLocation("minecraft","textures/gui/advancements/tabs.png");

    public static final ResourceLocation DEDICATED_CONTAINER_TEXTURE = new ResourceLocation(ModInfo.MOD_ID,"textures/gui/item_grid.png");
    public static final ResourceLocation DEDICATED_TABS = new ResourceLocation(ModInfo.MOD_ID,"textures/gui/tabs.png");
    public static final ResourceLocation ITEM_ENTRY_DISPLAY_RESOURCE = new ResourceLocation(ModInfo.MOD_ID,"textures/gui/item_entry.png");

    private static ResourceLocation getContainerTexture(){
        return ClientModInfo.getClientConfig().textureMode().get() == TextureMode.DEDICATED_LOCATION ? DEDICATED_CONTAINER_TEXTURE : CONTAINER_TEXTURE_RESOURCE;
    }

    private static ResourceLocation getTabsTexture(){
        return ClientModInfo.getClientConfig().textureMode().get() == TextureMode.DEDICATED_LOCATION ? DEDICATED_TABS : TABS_RESOURCE;
    }

    //target visual gap between the tab column and the frame's border. These differ because the
    //two border sprites (the simple 9-column box vs. the attached screen's stretched/capped
    //border for non-9 column counts) carry different amounts of built-in edge padding, so the
    //same raw offset doesn't produce the same visible gap in both places; tuned empirically.
    //Also used by ScreenFramework to position the tab column so the two stay in sync.
    public static final int MENU_TAB_GAP = 0;
    //was -7, tuned around a 7px inset that unselected tabs used to have before the mirroring
    //rework made them flush against the frame instead; that inset was hiding the real overlap
    //this value caused. 0 (flush) still overlapped the frame's own border by ~2px.
    public static final int ATTACHED_TAB_GAP = 2;

    public FromResource(ScreenFramework frameWork){
        super(frameWork);
    }

    public static class MenuMode extends FromResource{

        public MenuMode(ScreenFramework frameWork, ScreenRectangleWidgetParam pageSwitchTabParam) {
            super(frameWork);
            this.pageSwitchTabParam = pageSwitchTabParam;
        }

        @Override
        public void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
            super.renderBg(guiGraphics, partialTick, mouseX, mouseY);

            int baseRows = frameWork.menu instanceof EndlessInventoryMenu endless ? endless.getBaseRows() : rows;

            int startY = menuTop + 17 + baseRows*18;
            renderPlayerInv(guiGraphics,partialTick,mouseX,mouseY,menuLeft,startY);
        }

        private void renderPlayerInv(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY, int startX, int startY){
            guiGraphics.blit(getContainerTexture(), startX, startY,//WARN
                    0.0F, 126.0F, imageWidth, 96, 256, 256);
        }
    }

    public static class LeftLayout extends FromResource{

        public LeftLayout(ScreenFramework frameWork, ScreenRectangleWidgetParam pageSwitchTabParam){
            super(frameWork);
            this.pageSwitchTabParam = pageSwitchTabParam;
            this.mirrorTabs = true;
        }
    }

    public abstract class PagePainter implements PageBgRender {

        public abstract ResourceLocation texture();

        @Override
        public void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
            int startX = pageLeft;
            int startY = pageTop;

            if(columns!=9){
                renderSpecialBg(guiGraphics,partialTick,mouseX,mouseY,startX,startY);
            }else {
                guiGraphics.blit(texture(), startX, startY, 0, 0,
                        imageWidth, 17, 256, 256);
                startY += 17;
                int rowsToRender = rows;
                while (rowsToRender > 0) {
                    int height = 18 * Math.min(rowsToRender, 6);
                    guiGraphics.blit(texture(), startX, startY,
                            0.0F, 17.0F, imageWidth, height, 256, 256);
                    rowsToRender -= 6;
                    startY += height;
                }
                //the body tiling above always ends its last sampled row at v=124, which is that
                //row's own trailing highlight pixel (every row ends on one, per the source
                //texture). Sampling this cap starting at the same v=124 draws that highlight a
                //second time immediately below itself, showing up as a doubled/thicker line right
                //where the grid meets the cap. Starting one pixel later (v=125) and stretching 11
                //source pixels back up to the full 12px cap height avoids resampling that pixel.
                guiGraphics.blit(texture(), startX, pageTop+17+18*rows, imageWidth, 12, 0f, 125f, imageWidth, 11, 256, 256);
            }

        }

        private void renderSpecialBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY,
                                     int startX, int startY) {
            int initialX = startX;

            guiGraphics.blit(texture(), startX, startY, 0, 0,
                    7, 17, 256, 256);
            startX+=7;
            for (int columnsToRender = columns;columnsToRender>0;columnsToRender-=9) {
                int width = 18 * Math.min(9,columnsToRender);
                guiGraphics.blit(texture(), startX, startY, 7, 0,
                        width, 17, 256,256);

                startX+=width;
            }
            //u=168 is the highlight pixel that marks the end of the 9th body column (see the
            //per-row body cap below); at u=168 in this plain top-border row there's no such
            //highlight, so it's just excluded here to keep this cap's width consistent (7px,
            //matching the body-cap fix) rather than for any doubling reason.
            guiGraphics.blit(texture(), startX, startY, 7, 17, 169f, 0f, 7, 17, 256, 256);
            startX = initialX;

            startY+=17;
            for (int rowsToRender = rows;rowsToRender > 0;rowsToRender -= 6) {
                int height = 18*Math.min(rowsToRender,6);


                guiGraphics.blit(texture(), startX, startY, 0, 17,
                        7, height, 256, 256);
                startX+=7;
                for (int columnsToRender = columns;columnsToRender>0;columnsToRender-=9) {
                    int width = 18 * Math.min(9,columnsToRender);
                    guiGraphics.blit(texture(), startX, startY, 7, 17,
                            width, height, 256, 256);
                    startX+=width;
                }
                //u=168 is a highlight pixel marking the end of the 9th body column in the source
                //texture - when the body itself renders fewer than 9 columns, its own last column
                //already draws its own legitimate end-of-column highlight, so sampling this cap
                //starting at u=168 draws a second, unrelated highlight immediately next to it,
                //doubling into a visibly thicker line. Starting at u=169 instead skips it.
                guiGraphics.blit(texture(), startX, startY, 7, height, 169f, 17f, 7, height, 256, 256);
                startX = initialX;
                startY += height;
            }

            //same reasoning as the columns==9 path above: the body tiling's last sampled row
            //always ends at v=124 (that row's own trailing highlight pixel), so starting this cap
            //at the same v=124 doubles that highlight into a visibly thicker line. Sampling from
            //v=125 instead and stretching back up to the full 12px cap height avoids the overlap.
            guiGraphics.blit(texture(), startX, startY, 7, 12, 0f, 125f, 7, 11, 256, 256);
            startX+=7;
            for (int columnsToRender = columns;columnsToRender>0;columnsToRender-=9) {
                int width = 18 * Math.min(9,columnsToRender);
                guiGraphics.blit(texture(), startX, startY, width, 12, 7f, 125f, width, 11, 256, 256);

                startX+=width;
            }
            //same u=169 reasoning as the per-row body cap above, combined with the v=125 fix from
            //the bottom-left/middle caps.
            guiGraphics.blit(texture(), startX, startY, 7, 12, 169f, 125f, 7, 11, 256, 256);
            startX = initialX;
        }
    }

    @Override
    public Optional<PageBgRender> getDefaultPageBgRenderer() {
        return Optional.of(new PagePainter(){

            @Override
            public ResourceLocation texture() {
                return getContainerTexture();
            }
        });
    }

    public PagePainter dedicatePageBgRender(ResourceLocation texture){
        return new PagePainter() {
            @Override
            public ResourceLocation texture() {
                return texture;
            }
        };
    }

    /**
     * Draws a tab sprite, optionally mirrored horizontally. Mirroring samples the same source
     * region back-to-front (via a negative source width) rather than using different art, so a
     * sprite drawn for a left-side tab column (bevel/border facing right, toward the panel) comes
     * out facing left instead, matching a tab column on the panel's other side - without needing
     * a second copy of the texture, and it keeps working with any texture pack that replaces
     * tabs.png, since it's the same sprite the pack already had to draw for the unmirrored case.
     */
    private void blitTab(GuiGraphics guiGraphics, int destX, int destY, int destWidth, int destHeight,
                          float u, float v, int srcWidth, int srcHeight, boolean mirror) {
        if (mirror) {
            guiGraphics.blit(getTabsTexture(), destX, destY, destWidth, destHeight, u + srcWidth, v, -srcWidth, srcHeight, 256, 256);
        } else {
            guiGraphics.blit(getTabsTexture(), destX, destY, destWidth, destHeight, u, v, srcWidth, srcHeight, 256, 256);
        }
    }

    @Override
    public void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int pageX = pageSwitchTabParam.XPos();
        int pageY = pageSwitchTabParam.YPos();
        int selectedPageIndex = frameWork.getDisplayingPageIndex();
        for (int i = frameWork.firstPageIndex; i < frameWork.firstPageIndex + frameWork.pageBarCount; ++i) {
            if (i == selectedPageIndex) {
                //vanilla's real "selected" tab sprite: wider than the unselected one, with its
                //own border/bevel baked in, expanding toward whichever side isn't touching the
                //frame. It fills the whole 32px slot either way, so no position change is needed
                //here - mirroring handles the bevel direction.
                //the panel-facing edge of this sprite (the last few source pixels) is shaped to
                //connect into vanilla's *advancements* panel border specifically, which doesn't
                //match this mod's own box border - that mismatch is what showed up as a jagged,
                //not-quite-straight edge where the tab meets the frame. Sampling a slightly
                //narrower source region (stretched to the same destination width) crops that
                //connector out instead of trying to match it.
                //v=91 vs the first tab's v=92 is an extra row of the source strip's own top
                //padding: sampling from 91 pulls that padding row in above the actual tab art,
                //which is what made every non-first selected tab look shifted 1px lower than the
                //first one despite drawing at the same destY.
                if (i == 0) {
                    blitTab(guiGraphics, pageX, pageY, 32, 28, 0, 92, 29, 28, mirrorTabs);
                } else if (i == frameWork.firstPageIndex + frameWork.pageBarCount - 1) {
                    blitTab(guiGraphics, pageX, pageY, 32, 28, 64, 92, 29, 28, mirrorTabs);
                } else {
                    blitTab(guiGraphics, pageX, pageY, 32, 29, 32, 92, 29, 29, mirrorTabs);
                }
            } else {
                //unselected: flush against whichever side faces the frame, with a 1px stretch
                //into the outward side to close a hairline gap there (not re-sampled source
                //content, same reasoning as the mirrored selected sprites above).
                int contentX = mirrorTabs ? pageX : pageX + 7;
                blitTab(guiGraphics, contentX, pageY, 25, 27, 4, 64, 24, 27, mirrorTabs);
            }
            pageY+=28;
        }

        renderPageBarContent(guiGraphics, partialTick, mouseX, mouseY);
    }
}
