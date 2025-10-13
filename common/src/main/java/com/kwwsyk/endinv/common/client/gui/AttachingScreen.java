package com.kwwsyk.endinv.common.client.gui;

import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.common.SourceInventory;
import com.kwwsyk.endinv.common.client.CachedSrcInv;
import com.kwwsyk.endinv.common.client.gui.page.DisplayPage;
import com.kwwsyk.endinv.common.client.gui.page.manager.PageManager;
import com.kwwsyk.endinv.common.client.option.CachedConfig;
import com.kwwsyk.endinv.common.menu.page.pageManager.PageQuickMoveHandler;
import com.kwwsyk.endinv.common.network.payloads.PageData;
import com.kwwsyk.endinv.common.network.payloads.toServer.OpenEndInvPayload;
import com.kwwsyk.endinv.common.util.SortType;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;


/**The exceeded screen that controls interaction with Endless Inventory in client side.
 * Attached by an {@link AbstractContainerScreen<T>}
 * @param <T>
 *
 * @author Kay Zhang
 * @since 2025-6-4
 * @version 1.0.5
 */
public class AttachingScreen<T extends AbstractContainerMenu>{

    public static AttachingScreen<?> INSTANCE;

    private static final Logger LOGGER = LogUtils.getLogger();

    public final AbstractContainerScreen<T> screen;

    private ScreenFramework frameWork;

    private final PageManager pageMetadata = new PageManager() {

        @Override
        public AbstractContainerMenu getMenu() {
            return screen.getMenu();
        }

        @Override
        public SourceInventory getSourceInventory() {
            return CachedSrcInv.INSTANCE;
        }

        @Override
        public List<DisplayPage> getPages() {
            return pages;
        }

        @Override
        public DisplayPage getDisplayingPage() {
            return displayingPage;
        }

        @Override
        public void switchPageWithIndex(int index) {
            displayingPage = pages.get(index);
            CachedConfig.setDisplayingPageKey(getDisplayingPageId());
            displayingPage.initializeContents();
        }

        @Override
        public int rows() {
            return rows;
        }

        @Override
        public int columns() {
            return columns;
        }

        @Override
        public Player getPlayer() {
            return player;
        }

        @Override
        public int getItemSize() {
            return CachedSrcInv.INSTANCE.getItemSize();
        }

        @Override
        public int getMaxStackSize() {
            return CachedSrcInv.INSTANCE.getMaxItemStackSize();
        }

        @Override
        public boolean enableInfinity() {
            return CachedSrcInv.INSTANCE.isInfinityMode();
        }

        @Override
        public ItemStack quickMoveFromPage(ItemStack stack) {
            return quickMoveHandler.quickMoveFromPage(stack);
        }

        @Override
        public SortType sortType() {
            return sortType;
        }

        @Override
        public void setSortType(SortType sortType) {
            AttachingScreen.this.sortType = sortType;
        }

        @Override
        public boolean isSortReversed() {
            return reverseSort;
        }

        @Override
        public void switchSortReversed() {
            reverseSort = !reverseSort;
        }

        @Override
        public void setSortReversed(boolean reversed) {
            reverseSort = reversed;
        }

        @Override
        public String searching() {
            return searching;
        }

        @Override
        public void setSearching(String searching) {
            AttachingScreen.this.searching = searching;
        }

        @Override
        public void sendEndInvData() {//do nop as in the client
        }

    };

    private final PageQuickMoveHandler quickMoveHandler;


    private DisplayPage displayingPage;

    public final List<DisplayPage> pages;

    private final int rows;

    private final int columns;

    public SortType sortType;

    public String searching;

    private final Player player;

    private boolean reverseSort;

    //invoked when an ACS is created or initialized
    public AttachingScreen(AbstractContainerScreen<T> screen){
        this.screen = screen;
        this.pages = pageMetadata.buildPages();
        assert Minecraft.getInstance().player != null;
        this.player = Minecraft.getInstance().player;
        PageData data = CachedConfig.resolveLayout(screen, false);
        this.rows = data.rows();
        this.columns = data.columns();
        this.sortType = data.sortType();
        this.searching = data.search();
        this.reverseSort = data.reverseSort();
        this.pageMetadata.switchPageWithId(data.pageRegKey());
        this.quickMoveHandler = new PageQuickMoveHandler(this.pageMetadata);
        INSTANCE = this;
    }

    //invoked when an ACS is initialized
    public void init(IScreenEvent event){
        this.frameWork = ScreenFramework.getInstance()==null ? new ScreenFramework(this) : ScreenFramework.getInstance();
        frameWork.addWidgetToScreen(event::addListener);
        // Ensure server-side manager attaches for the current menu so actions like quick-move are authoritative
        CachedConfig.readAndSyncClientConfigToServer(false);
        ModInfo.getPacketDistributor().sendToServer(new OpenEndInvPayload(false, rows));
    }

    public void renderPre(IScreenEvent event) {
    }

    public void render(IScreenEvent event) {
        int mouseX = (int) event.getMouseX();
        int mouseY = (int) event.getMouseY();
        GuiGraphics guiGraphics = event.getGuiGraphics();
        float partialTick = event.getPartialTick();

        frameWork.renderBg(guiGraphics,mouseX,mouseY,partialTick);
        frameWork.render(guiGraphics,mouseX,mouseY,partialTick);
    }


    public void mouseClicked(IScreenEvent event) {
        double mouseX = event.getMouseX();
        double mouseY = event.getMouseY();
        int keyCode = event.getButton();
        boolean isActionOverride = frameWork.mouseClicked(mouseX,mouseY,keyCode);
        event.setCanceled(isActionOverride);
    }


    public void mouseReleased(IScreenEvent event) {
        double mouseX = event.getMouseX();
        double mouseY = event.getMouseY();
        int keyCode = event.getButton();
        event.setCanceled(frameWork.mouseReleased(mouseX,mouseY,keyCode));
    }

    public void mouseDragged(IScreenEvent event) {
        double mouseX = event.getMouseX();
        double mouseY = event.getMouseY();
        int button = event.getMouseButton();
        double dragX = event.getDragX();
        double dragY = event.getDragY();

        event.setCanceled(frameWork.mouseDragged(mouseX,mouseY,button,dragX,dragY));
    }

    public void mouseScrolled(IScreenEvent event) {
        double scrollY = event.getScrollDeltaY();
        frameWork.mouseScrolled(event.getMouseX(),event.getMouseY(),scrollY);
    }

    public void keyPressed(IScreenEvent event) {
        int keyCode = event.getKeyCode();
        int scanCode = event.getScanCode();
        int modifiers = event.getModifiers();
        event.setCanceled(frameWork.keyPressed(keyCode,scanCode,modifiers));
    }

    public void charTyped(IScreenEvent event) {
        char codePoint = event.getCodePoint();
        int modifiers = event.getModifiers();
        event.setCanceled(frameWork.charTyped(codePoint,modifiers));
    }

    public void closed(IScreenEvent event){
        frameWork.onClose();
        close("On attached screen closing");
    }

    public void close(@Nullable String reason){
        INSTANCE = null;
        LOGGER.info("Attached Screen {} closed with reason: {}", this, reason);
    }

    public PageManager getPageManager() {
        return pageMetadata;
    }

    public AbstractContainerScreen<?> getScreen() {
        return screen;
    }

    public ScreenFramework getFrameWork(){
        return frameWork;
    }

    public List<Rect2i> getArea(){
        return List.of(new Rect2i(frameWork.leftPos, frameWork.topPos, frameWork.imageWidth, frameWork.imageHeight));
    }
}















