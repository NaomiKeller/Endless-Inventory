package com.kwwsyk.endinv.fabric.client.events;

import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.common.client.ClientSyncedConfig;
import com.kwwsyk.endinv.common.client.gui.AttachedScreen;
import com.kwwsyk.endinv.common.client.gui.EndlessInventoryScreen;
import com.kwwsyk.endinv.common.client.gui.IScreenEvent;
import com.kwwsyk.endinv.common.network.payloads.SyncedConfig;
import com.kwwsyk.endinv.common.network.payloads.toServer.OpenEndInvPayload;
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
    private static AttachedScreen<?> attachment;

    private ScreenAttachment() {
    }

    public static void register() {
        ScreenEvents.BEFORE_INIT.register((client, screen, width, height) -> {
            if (screen instanceof AbstractContainerScreen<?>) {
                ClientSyncedConfig.readAndSyncClientConfigToServer(false);
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

        SyncedConfig syncedConfig = getSyncedConfig().getWith(player);
        if (!syncedConfig.checkForAttaching()) {
            attachment = null;
            return;
        }

        ClientSyncedConfig.readAndSyncClientConfigToServer(false);

        if (attachment == null || attachment.screen != screen) {
            ModInfo.getPacketDistributor().sendToServer(new OpenEndInvPayload(false));
            attachment = new AttachedScreen<>(container);
        }

            AttachedScreen<?> current = attachment;
            current.init(new IScreenEvent() {
                @Override
                public void addListener(AbstractWidget widget) {
                    screen.addRenderableWidget(widget);
                }
            });

            ScreenEvents.remove(screen).register(() -> detach(current));
            ScreenEvents.beforeRender(screen).register((s, graphics, mouseX, mouseY, delta) -> preRender(current));
            ScreenEvents.afterRender(screen).register((s, graphics, mouseX, mouseY, delta) -> render(current, graphics, mouseX, mouseY, delta));

            ScreenMouseEvents.allowMouseClick(screen).register((s, mouseX, mouseY, button) -> allowMouseClick(current, mouseX, mouseY, button));
            ScreenMouseEvents.allowMouseRelease(screen).register((s, mouseX, mouseY, button) -> allowMouseRelease(current, mouseX, mouseY, button));
            ScreenMouseEvents.allowMouseDrag(screen).register((s, mouseX, mouseY, button, deltaX, deltaY) -> allowMouseDrag(current, mouseX, mouseY, button, deltaX, deltaY));
            ScreenMouseEvents.allowMouseScroll(screen).register((s, mouseX, mouseY, horizontal, vertical) -> allowMouseScroll(current, mouseX, mouseY, horizontal, vertical));

            ScreenKeyboardEvents.allowKeyPress(screen).register((s, keyCode, scanCode, modifiers) -> allowKeyPress(current, keyCode, scanCode, modifiers));
            ScreenKeyboardEvents.allowCharTyped(screen).register((s, chr, modifiers) -> allowCharTyped(current, chr, modifiers));
        });
    }

    private static void detach(AttachedScreen<?> expected) {
        if (attachment == expected) {
            expected.closed(new IScreenEvent() {
            });
            attachment = null;
        }
    }

    private static void preRender(AttachedScreen<?> expected) {
        if (attachment == expected) {
            expected.renderPre(new IScreenEvent() {
            });
        }
    }

    private static void render(AttachedScreen<?> expected, GuiGraphics graphics, double mouseX, double mouseY, float delta) {
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

    private static boolean allowMouseClick(AttachedScreen<?> expected, double mouseX, double mouseY, int button) {
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

    private static boolean allowMouseRelease(AttachedScreen<?> expected, double mouseX, double mouseY, int button) {
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

    private static boolean allowMouseDrag(AttachedScreen<?> expected, double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (attachment != expected || !isAttachmentActive(expected)) {
            return true;
        }
        boolean[] canceled = {false};
        expected.mouseDragged(new IScreenEvent() {
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
        return !canceled[0];
    }

    private static boolean allowMouseScroll(AttachedScreen<?> expected, double mouseX, double mouseY, double horizontal, double vertical) {
        if (attachment != expected || !isAttachmentActive(expected)) {
            return true;
        }
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
        });
        return true;
    }

    private static boolean allowKeyPress(AttachedScreen<?> expected, int keyCode, int scanCode, int modifiers) {
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

    private static boolean allowCharTyped(AttachedScreen<?> expected, char chr, int modifiers) {
        if (attachment != expected || !isAttachmentActive(expected)) {
            return true;
        }
        boolean[] canceled = {false};
        expected.charTyped(new IScreenEvent() {
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
        return !canceled[0];
    }

    private static boolean isAttachmentActive(AttachedScreen<?> expected) {
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
        return true;
    }
}
