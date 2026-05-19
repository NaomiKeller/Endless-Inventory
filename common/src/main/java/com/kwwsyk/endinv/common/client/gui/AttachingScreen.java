package com.kwwsyk.endinv.common.client.gui;

import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.common.client.ClientModInfo;
import com.kwwsyk.endinv.common.client.gui.bg.IRectangleParam;
import com.kwwsyk.endinv.common.client.option.ClientConfigs;
import com.kwwsyk.endinv.common.client.option.MenuAttachabilityCache;
import com.kwwsyk.endinv.common.network.payloads.toServer.OpenEndInvPayload;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;

import static com.kwwsyk.endinv.common.ModRegistries.NbtAttachments.getSyncedConfig;


/**The exceeded screen that controls interaction with Endless Inventory in client side.
 * Attached by an {@link AbstractContainerScreen<T>}
 * @param <T>
 *
 * @author Kay Zhang
 * @since 2025-6-4
 * @version 1.0.5
 */
public class AttachingScreen<T extends AbstractContainerMenu>{
    @Nullable
    public static AttachingScreen<?> INSTANCE;

    private static final Logger LOGGER = LogUtils.getLogger();

    public final AbstractContainerScreen<T> screen;
    public final T menu;

    private ScreenFramework frameWork;
    private boolean temporarilyDisabled;

    //invoked when an ACS is created or initialized
    public AttachingScreen(AbstractContainerScreen<T> screen){
        this.screen = screen;
        this.menu = screen.getMenu();
        INSTANCE = this;
    }
    @SuppressWarnings("nullable")
    public static boolean isAttachable(AbstractContainerScreen<?> screen){
        try{
            return ClientConfigs.DO_ATTACH.get()
                    && getSyncedConfig().computeIfAbsent(Minecraft.getInstance().player).checkForAttaching()
                    && ClientConfigs.SPECIFIED_ATTACHABILITY.get().isMenuAttachable(screen.getMenu())
                    && MenuAttachabilityCache.isAttachable(screen);
        } catch (RuntimeException e){
            LOGGER.error("", e);
        }
        return false;
    }

    public static net.minecraft.client.gui.components.Button configButton(Screen screen, IRectangleParam configButtonParam,
                                                                          Runnable open, Runnable close) {
        Minecraft mc = Minecraft.getInstance();
        return Button.builder(
                Component.literal("⚙"),
                        btn -> {
                        if(Screen.hasShiftDown()){
                            mc.setScreen(ClientModInfo.createConfigScreen(screen));
                        } else {
                            boolean currentA = ClientConfigs.DO_ATTACH.get();
                            currentA = !currentA;
                            ClientConfigs.DO_ATTACH.set(currentA);
                            if(currentA){
                                open.run();
                            }else {
                                close.run();
                            }
                        }
                })
                .pos(configButtonParam.x(), configButtonParam.y())
                .size(configButtonParam.width(), configButtonParam.height())
                .tooltip(Tooltip.create(Component.translatableWithFallback("endinv.configbutton", "Toggle endinv attached menu, shift for settings.")))
                .build();
    }

    //invoked when an ACS is initialized
    public void init(IScreenEvent event){
        this.frameWork = ScreenFramework.getInstance()==null ? new ScreenFramework(this) : ScreenFramework.getInstance();
        frameWork.addWidgetToScreen(event::addListener);
        // Ensure server-side manager attaches for the current menu so actions like quick-move are authoritative
        ModInfo.getPacketDistributor().sendToServer(new OpenEndInvPayload(false, frameWork.rows()));
    }

    public void renderPre(IScreenEvent event) {
    }

    public void render(IScreenEvent event) {
        if(temporarilyDisabled) return;
        int mouseX = (int) event.getMouseX();
        int mouseY = (int) event.getMouseY();
        GuiGraphics guiGraphics = event.getGuiGraphics();
        float partialTick = event.getPartialTick();

        frameWork.renderBg(guiGraphics, mouseX, mouseY, partialTick);
        frameWork.render(guiGraphics,mouseX,mouseY,partialTick);
    }


    public void mouseClicked(IScreenEvent event) {
        if(temporarilyDisabled) return;
        double mouseX = event.getMouseX();
        double mouseY = event.getMouseY();
        int keyCode = event.getButton();
        boolean isActionOverride = frameWork.mouseClicked(mouseX,mouseY,keyCode);
        event.setCanceled(isActionOverride);
    }


    public void mouseReleased(IScreenEvent event) {
        if(temporarilyDisabled) return;
        double mouseX = event.getMouseX();
        double mouseY = event.getMouseY();
        int keyCode = event.getButton();
        event.setCanceled(frameWork.mouseReleased(mouseX,mouseY,keyCode));
    }

    public void mouseDragged(IScreenEvent event) {
        if(temporarilyDisabled) return;
        double mouseX = event.getMouseX();
        double mouseY = event.getMouseY();
        int button = event.getMouseButton();
        double dragX = event.getDragX();
        double dragY = event.getDragY();

        event.setCanceled(frameWork.mouseDragged(mouseX,mouseY,button,dragX,dragY));
    }

    public void mouseScrolled(IScreenEvent event) {
        if(temporarilyDisabled) return;
        double scrollY = event.getScrollDeltaY();
        frameWork.mouseScrolled(event.getMouseX(),event.getMouseY(),scrollY);
    }

    public void keyPressed(IScreenEvent event) {
        if(temporarilyDisabled) return;
        int keyCode = event.getKeyCode();
        int scanCode = event.getScanCode();
        int modifiers = event.getModifiers();
        event.setCanceled(frameWork.keyPressed(keyCode,scanCode,modifiers));
    }

    public void charTyped(IScreenEvent event) {
        if(temporarilyDisabled) return;
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















