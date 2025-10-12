package com.kwwsyk.endinv.fabric.client.events;

import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.common.client.ClientModInfo;
import com.kwwsyk.endinv.common.client.gui.AttachingScreen;
import com.kwwsyk.endinv.common.client.gui.EndlessInventoryScreen;
import com.kwwsyk.endinv.common.client.gui.IScreenEvent;
import com.kwwsyk.endinv.common.client.option.CachedConfig;
import com.kwwsyk.endinv.common.client.option.MenuAttachabilityCache;
import com.kwwsyk.endinv.common.network.payloads.toServer.OpenEndInvPayload;
import com.kwwsyk.endinv.fabric.mixin.ScreenAccessor;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

import static com.kwwsyk.endinv.common.ModRegistries.NbtAttachments.getSyncedConfig;

public final class ScreenAttachment {

    @Nullable
    public static AttachingScreen<?> attachment;

    private ScreenAttachment() {
    }

    public static void register() {
        ScreenEvents.BEFORE_INIT.register((client, screen, width, height) -> {
            if (screen instanceof AbstractContainerScreen<?>) {
                CachedConfig.readAndSyncClientConfigToServer(false);
            }
        });

        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (!(screen instanceof AbstractContainerScreen<?> container) || screen instanceof EndlessInventoryScreen) {
                return;
            }

            Player player = client.player;
            if (player == null) {
                attachment = null;
                return;
            }

            if (!ClientModInfo.getClientConfig().attaching().get()) {
                attachment = null;
                return;
            }
            if (!MenuAttachabilityCache.isAttachable(container)) {
                attachment = null;
                return;
            }

            //CachedConfig.readAndSyncClientConfigToServer(false);

            if (attachment == null) {
                ModInfo.getPacketDistributor().sendToServer(new OpenEndInvPayload());
                attachment = new AttachingScreen<>(container);
            }

            attachment.init(new IScreenEvent() {
                @Override
                public void addListener(AbstractWidget widget) {
                    ((ScreenAccessor) screen).endinv$invokeAddRenderableWidget(widget);
                }
            });

            ScreenEvents.remove(screen).register(s -> {
                if(attachment!=null){
                    attachment.closed(new IScreenEvent() {});
                    attachment = null;
                }
            });

            ScreenEvents.beforeRender(screen).register((s, graphics, mouseX, mouseY, delta) -> preRender(attachment));
            ScreenEvents.afterRender(screen).register((s, graphics, mouseX, mouseY, delta) -> render(attachment, graphics, mouseX, mouseY, delta));

            ScreenMouseEvents.allowMouseClick(screen).register((s, mouseX, mouseY, button) -> allowMouseClick(attachment, mouseX, mouseY, button));
            ScreenMouseEvents.allowMouseRelease(screen).register((s, mouseX, mouseY, button) -> allowMouseRelease(attachment, mouseX, mouseY, button));
            ScreenMouseEvents.allowMouseScroll(screen).register((s, mouseX, mouseY, horizontal, vertical) -> allowMouseScroll(attachment, mouseX, mouseY, horizontal, vertical));

            ScreenKeyboardEvents.allowKeyPress(screen).register((s, keyCode, scanCode, modifiers) -> allowKeyPress(attachment, keyCode, scanCode, modifiers));
        });
    }

    private static void preRender(AttachingScreen<?> expected) {
        expected.renderPre(new IScreenEvent() {});
    }

    private static void render(AttachingScreen<?> expected, GuiGraphics graphics, double mouseX, double mouseY, float delta) {
        if (attachment != expected) {
            return;
        }
        expected.render(new IScreenEvent() {
            @Override
            public double getMouseX() {
                return mouseX;
            }

            @Override
            public double getMouseY() {
                return mouseY;
            }

            @Override
            public float getPartialTick() {
                return delta;
            }

            @Override
            public GuiGraphics getGuiGraphics() {
                return graphics;
            }
        });
    }

    private static boolean allowMouseClick(AttachingScreen<?> expected, double mouseX, double mouseY, int button) {
        if (attachment != expected || !isAttachmentActive(expected)) {
            return true;
        }
        boolean[] canceled = {false};
        expected.mouseClicked(new IScreenEvent() {
            @Override
            public double getMouseX() {
                return mouseX;
            }

            @Override
            public double getMouseY() {
                return mouseY;
            }

            @Override
            public int getButton() {
                return button;
            }

            @Override
            public void setCanceled(boolean flag) {
                canceled[0] = flag;
            }
        });
        return !canceled[0];
    }

    private static boolean allowMouseRelease(AttachingScreen<?> expected, double mouseX, double mouseY, int button) {
        if (attachment != expected || !isAttachmentActive(expected)) {
            return true;
        }
        boolean[] canceled = {false};
        expected.mouseReleased(new IScreenEvent() {
            @Override
            public double getMouseX() {
                return mouseX;
            }

            @Override
            public double getMouseY() {
                return mouseY;
            }

            @Override
            public int getButton() {
                return button;
            }

            @Override
            public void setCanceled(boolean flag) {
                canceled[0] = flag;
            }
        });
        return !canceled[0];
    }

    private static boolean allowMouseScroll(AttachingScreen<?> expected, double mouseX, double mouseY, double horizontal, double vertical) {
        if (attachment != expected || !isAttachmentActive(expected)) {
            return true;
        }
        boolean[] canceled = new boolean[]{false};
        expected.mouseScrolled(new IScreenEvent() {
            @Override
            public double getMouseX() {
                return mouseX;
            }

            @Override
            public double getMouseY() {
                return mouseY;
            }

            @Override
            public double getScrollDeltaY() {
                return vertical;
            }

            @Override
            public double getScrollDeltaX() {
                return horizontal;
            }

            @Override
            public void setCanceled(boolean canceled1){
                canceled[0] = canceled1;
            }
        });
        return !canceled[0];
    }

    private static boolean allowKeyPress(AttachingScreen<?> expected, int keyCode, int scanCode, int modifiers) {
        if (attachment != expected || !isAttachmentActive(expected)) {
            return true;
        }
        boolean[] canceled = {false};
        expected.keyPressed(new IScreenEvent() {
            @Override
            public int getKeyCode() {
                return keyCode;
            }

            @Override
            public int getScanCode() {
                return scanCode;
            }

            @Override
            public int getModifiers() {
                return modifiers;
            }

            @Override
            public void setCanceled(boolean flag) {
                canceled[0] = flag;
            }
        });
        return !canceled[0];
    }

    public static boolean handleMouseDrag(AbstractContainerScreen<?> screen, double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        AttachingScreen<?> current = attachment;
        if (current == null || current.screen != screen || !isAttachmentActive(current)) {
            return false;
        }
        boolean[] canceled = {false};
        current.mouseDragged(new IScreenEvent() {
            @Override
            public double getMouseX() {
                return mouseX;
            }

            @Override
            public double getMouseY() {
                return mouseY;
            }

            @Override
            public int getMouseButton() {
                return button;
            }

            @Override
            public double getDragX() {
                return deltaX;
            }

            @Override
            public double getDragY() {
                return deltaY;
            }

            @Override
            public void setCanceled(boolean flag) {
                canceled[0] = flag;
            }
        });
        return canceled[0];
    }

    public static boolean handleCharTyped(Screen screen, char chr, int modifiers) {
        AttachingScreen<?> current = attachment;
        if (current == null || current.screen != screen || !isAttachmentActive(current)) {
            return false;
        }
        boolean[] canceled = {false};
        current.charTyped(new IScreenEvent() {
            @Override
            public char getCodePoint() {
                return chr;
            }

            @Override
            public int getModifiers() {
                return modifiers;
            }

            @Override
            public void setCanceled(boolean flag) {
                canceled[0] = flag;
            }
        });
        return canceled[0];
    }

    private static boolean isAttachmentActive(AttachingScreen<?> expected) {
        Screen screen = Minecraft.getInstance().screen;
        if (!(screen instanceof AbstractContainerScreen<?>)) {
            attachment = null;
            return false;
        }
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            attachment = null;
            return false;
        }
        if (expected.screen != screen) {
            return false;
        }
        if (!getSyncedConfig().getWith(player).attaching()) {
            attachment = null;
            return false;
        }
        if (!MenuAttachabilityCache.isAttachable((AbstractContainerScreen<?>) screen)) {
            attachment = null;
            return false;
        }
        return true;
    }
}


