package com.kwwsyk.endinv.common.client.gui.page;

import com.kwwsyk.endinv.common.client.ClientModInfo;
import com.kwwsyk.endinv.common.client.gui.EndlessInventoryScreen;
import com.kwwsyk.endinv.common.client.gui.bg.FromResource;
import com.kwwsyk.endinv.common.client.gui.bg.SFBgRenderer;
import com.kwwsyk.endinv.common.client.option.TextureMode;
import com.kwwsyk.endinv.common.menu.page.PageType;
import com.kwwsyk.endinv.common.menu.page.pageManager.PageMetaDataManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ItemEntryDisplay extends ItemDisplay{

    private static final int TOOLTIP_X_SEP = 5;

    /**
     * Used to disable Item name rendering.
     */
    protected boolean jmpTooltip1st = true;

    protected SFBgRenderer.PageBgRender renderer = null;

    public ItemEntryDisplay(PageType pageType, PageMetaDataManager metaDataManager) {
        super(pageType,metaDataManager);
        this.length = meta.rows();
    }

    @Override
    public void scrollTo(float pos) {
        int startIndex = getRowIndexForScroll(pos);
        this.initializeContents(startIndex,this.length);
    }

    @Override
    public void initializeContents(int startIndex, int length) {
        this.startIndex = startIndex;
        this.length = Math.min(length, meta.rows());
        if(items==null || length!=this.items.size()){
            this.items = NonNullList.withSize(length,ItemStack.EMPTY);
        }
        release();
        requestRemoteContents();
    }

    public void toggleJmpItemName(boolean jmpTooltip1st){
        this.jmpTooltip1st = jmpTooltip1st;
    }

    @Override
    protected boolean clickedInOneSlot(double XOffset, double YOffset, double lastX, double lastY) {
        return (int)YOffset/18 == (int)lastY/18;
    }

    @Override
    public int getSlotForMouseOffset(double XOffset, double YOffset) {
        if(XOffset<0||YOffset<0||XOffset>18* meta.columns()||YOffset>18* meta.rows()) return -1;
        return (int)YOffset/18;
    }

    @Override
    protected boolean isHiddenBySortBox(int rowIndex, int columnIndex) {
        return rowIndex<=2 && Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> screen && (
                screen instanceof EndlessInventoryScreen EIS && EIS.getFrameWork().sortTypeSwitchBox.isOpen()
                        || framework.sortTypeSwitchBox.isOpen()
        );
    }

    @Override
    public void renderPage(GuiGraphics guiGraphics) {

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
        int rowIndex = 0;
        int columnIndex = 0;
        for(ItemStack stack : items){
            guiGraphics.renderItem(stack,leftPos,topPos+rowIndex*18+1,columnIndex+rowIndex<<8);
            if(!stack.isEmpty())
                renderItemEntry(stack,leftPos+18,topPos+rowIndex*18+5,guiGraphics);
            if(!isHiddenBySortBox(rowIndex,columnIndex))
                guiGraphics.renderItemDecorations(Minecraft.getInstance().font, stack, leftPos,topPos+rowIndex*18+1, getDisplayAmount(stack));
            rowIndex++;
            if(rowIndex>= meta.rows()) break;
        }
        guiGraphics.pose().popPose();
    }

    private void renderItemEntry(ItemStack item, int x, int y, GuiGraphics graphics){
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        List<Component> tooltips = AbstractContainerScreen.getTooltipFromItem(mc,item);
        int strX = x;
        boolean jmp = jmpTooltip1st;
        for(var tip : tooltips){
            if(jmp){
                jmp = false;
                continue;
            }
            Component tip1 = Component.literal(tip.getString());
            int strX1 = strX + font.width(tip.getVisualOrderText());
            if(strX1 >= x + meta.columns()*18 -18-3){
                graphics.drawString(font,Component.literal("..."),strX,y,0xFFFFFF00);
                break;
            }
            graphics.drawString(font,tip1,strX,y,0xFFFFFF00);
            strX = strX1 + TOOLTIP_X_SEP;
        }
    }

    @Override
    public void renderHovering(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderSlotHighlight(graphics, mouseX, mouseY, partialTick);
        int hoveringSlot = getSlotForMouseOffset(mouseX-leftPos,mouseY-topPos);
        if(hoveringSlot>=0&&hoveringSlot<items.size()){
            ItemStack hovering = items.get(hoveringSlot);
            if(hovering.isEmpty()) return;
            graphics.pose().pushPose();
            graphics.pose().translate(0,0,550.0F);
            graphics.renderTooltip(Minecraft.getInstance().font,
                    AbstractContainerScreen.getTooltipFromItem(Minecraft.getInstance(),hovering),
                    hovering.getTooltipImage(),
                    mouseX, mouseY);
            graphics.pose().popPose();
        }
    }

    protected void renderSlotHighlight(GuiGraphics graphics, int mouseX, int mouseY, float partialTick){
        for(int v = 0; v< meta.rows(); ++v){
            int x1 = leftPos;
            int x2 = leftPos + meta.columns()*18 -2;
            int y1 = topPos+18*v+1;
            int y2 = topPos+18*v+18;
            if(mouseX>x1 && mouseX<x2 && mouseY>y1 && mouseY<y2){
                if(!meta.getMenu().getCarried().isEmpty()) return;
                graphics.fillGradient(RenderType.guiOverlay(),x1,y1,x2,y2,0x80ffffff,0x80ffffff,0);
            }
        }
    }

    @Override
    public void renderBg(SFBgRenderer SFBgRenderer, GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        if (this.renderer == null) renderer = bgRender(SFBgRenderer);
        renderer.renderBg(guiGraphics, partialTicks, mouseX, mouseY);
    }

    protected SFBgRenderer.PageBgRender bgRender(SFBgRenderer sfBgRenderer){
        TextureMode mode = ClientModInfo.getClientConfig().textureMode().get();
        if(sfBgRenderer instanceof FromResource fromResource){
            if(mode == TextureMode.DEDICATED_LOCATION) return fromResource.dedicatePageBgRender(FromResource.ITEM_ENTRY_DISPLAY_RESOURCE);
            return (guiGraphics, partialTicks, mouseX, mouseY) ->{// assert mode = from_resource
                sfBgRenderer.getDefaultPageBgRenderer().ifPresent(bgRenderer -> bgRenderer.renderBg(guiGraphics, partialTicks, mouseX, mouseY));
                int pageX = sfBgRenderer.getScreenFrameWork().getPageX();
                int startY = sfBgRenderer.getScreenFrameWork().getPageY();
                for(int i = 0; i< meta.rows(); ++i){
                    guiGraphics.fill(pageX,startY,pageX+18* meta.columns()-2,startY+1,0xFF373737);
                    guiGraphics.fill(pageX,startY+1,pageX+18* meta.columns()-2,startY+17,0xFF8b8b8b);
                    guiGraphics.fill(pageX,startY+17,pageX+18* meta.columns()-2,startY+18,0xFFFFFFFF);
                    startY+=18;
                }
            };
        }
        return (guiGraphics, partialTicks, mouseX, mouseY) ->{//assert mode = transparent
            int pageX = sfBgRenderer.getScreenFrameWork().getPageX();
            int startY = sfBgRenderer.getScreenFrameWork().getPageY();
            for(int i = 0; i< meta.rows(); ++i){
                guiGraphics.fill(pageX,startY,pageX+18* meta.columns()-2,startY+1,0xFF373737);
                guiGraphics.fill(pageX,startY+1,pageX+18* meta.columns()-2,startY+17,0x37606037);
                guiGraphics.fill(pageX,startY+17,pageX+18* meta.columns()-2,startY+18,0xFFFFFFFF);
                startY+=18;
            }
        };
    }
}
