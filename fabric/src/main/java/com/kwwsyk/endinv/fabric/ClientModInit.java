package com.kwwsyk.endinv.fabric;

import com.kwwsyk.endinv.common.AbstractClientModInitializer;
import com.kwwsyk.endinv.common.client.IContainerScreenHelper;
import com.kwwsyk.endinv.common.client.IInputHandler;
import com.kwwsyk.endinv.common.client.KeyMappings;
import com.kwwsyk.endinv.common.client.option.IClientConfig;
import com.kwwsyk.endinv.fabric.client.FabricClientConfig;
import com.kwwsyk.endinv.fabric.client.events.ClientEvents;
import com.kwwsyk.endinv.fabric.mixin.AbstractContainerScreenAccessor;
import com.kwwsyk.endinv.fabric.network.FabricNetworking;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class ClientModInit extends AbstractClientModInitializer implements ClientModInitializer {

    private static final Map<KeyMappings.EndInvKey, KeyMapping> REGISTERED_KEYS = new HashMap<>();

    @Override
    public void onInitializeClient() {
        registerKey(KeyMappings.OPEN_MENU);
        registerEmptyKey(KeyMappings.QUICK_MOVE);
        registerKey(KeyMappings.STAR_ITEM);
        FabricNetworking.initClient();
        ClientEvents.register();
    }

    @Override
    protected IClientConfig loadClientConfig() {
        return FabricClientConfig.INSTANCE;
    }

    @Override
    protected IInputHandler getInputHandler() {
        return new IInputHandler() {
            @Override
            public boolean isActiveAndMatches(KeyMapping keyMapping, InputConstants.Key key) {
                return matchesKey(keyMapping, key);
            }

            @Override
            public boolean isActiveAndMatches(KeyMappings.EndInvKey endInvKey, InputConstants.Key key) {
                KeyMapping mapping = REGISTERED_KEYS.computeIfAbsent(endInvKey, ClientModInit::registerKey);
                if (!conditionMatches(endInvKey)) {
                    return false;
                }

                boolean boundMatch = matchesKey(mapping, key);
                if (boundMatch) {
                    // If user set a dedicated key, do not require extra CTRL.
                    if (endInvKey.modifier() == KeyMappings.Modifier.CTRL && endInvKey != KeyMappings.QUICK_MOVE) {
                        return Screen.hasControlDown();
                    }
                    return true;
                }

                // Fallback: if no dedicated key matched and this is QUICK_MOVE,
                // allow Ctrl + Left Click inside GUI.
                if (endInvKey == KeyMappings.QUICK_MOVE
                        && key.getType() == InputConstants.Type.MOUSE
                        && key.getValue() == GLFW.GLFW_MOUSE_BUTTON_1) {
                    return Screen.hasControlDown();
                }
                return false;
            }
        };
    }

    @Override
    protected IContainerScreenHelper getScreenHelper() {
        return new IContainerScreenHelper() {
            @Override
            public int getGuiLeft(AbstractContainerScreen<?> screen) {
                return ((AbstractContainerScreenAccessor) screen).endinv$getLeftPos();
            }

            @Override
            public int getGuiTop(AbstractContainerScreen<?> screen) {
                return ((AbstractContainerScreenAccessor) screen).endinv$getTopPos();
            }

            @Override
            public int getGuiXSize(AbstractContainerScreen<?> screen) {
                return ((AbstractContainerScreenAccessor) screen).endinv$getImageWidth();
            }

            @Override
            public int getGuiYSize(AbstractContainerScreen<?> screen) {
                return ((AbstractContainerScreenAccessor) screen).endinv$getImageHeight();
            }
        };
    }

    private static boolean matchesKey(KeyMapping mapping, InputConstants.Key key) {
        InputConstants.Type type = key.getType();
        if (type == InputConstants.Type.MOUSE) {
            return mapping.matchesMouse(key.getValue());
        }
        return mapping.matches(key.getValue(), key.getValue());
    }

    private static boolean conditionMatches(KeyMappings.EndInvKey key) {
        return switch (key.condition()) {
            case GUI -> Minecraft.getInstance().screen != null;
            case IN_GAME -> Minecraft.getInstance().screen == null;
        };
    }

    private static KeyMapping registerKey(KeyMappings.EndInvKey key) {
        KeyMapping mapping = new KeyMapping(key.key(), key.type(), key.keyCode(), KeyMappings.CATEGORY);
        KeyBindingHelper.registerKeyBinding(mapping);
        REGISTERED_KEYS.put(key, mapping);
        return mapping;
    }

    private static KeyMapping registerEmptyKey(KeyMappings.EndInvKey key) {
        KeyMapping mapping = new KeyMapping(key.key(), InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, KeyMappings.CATEGORY);
        KeyBindingHelper.registerKeyBinding(mapping);
        REGISTERED_KEYS.put(key, mapping);
        return mapping;
    }

    public static KeyMapping getRegisteredKey(KeyMappings.EndInvKey key) {
        return REGISTERED_KEYS.get(key);
    }
}
