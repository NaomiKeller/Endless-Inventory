package com.kwwsyk.endinv.fabric;

import com.kwwsyk.endinv.common.AbstractClientModInitializer;
import com.kwwsyk.endinv.common.client.IContainerScreenHelper;
import com.kwwsyk.endinv.common.client.IInputHandler;
import com.kwwsyk.endinv.common.client.KeyMappings;
import com.kwwsyk.endinv.common.client.option.ClientConfigs;
import com.kwwsyk.endinv.common.options.config.json.JsonConfigurationHandler;
import com.kwwsyk.endinv.fabric.client.events.ClientEvents;
import com.kwwsyk.endinv.fabric.mixin.AbstractContainerScreenAccessor;
import com.kwwsyk.endinv.fabric.network.FabricClientNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.jetbrains.annotations.Nullable;

import static com.kwwsyk.endinv.common.client.KeyMappings.*;

public class ClientModInit extends AbstractClientModInitializer implements ClientModInitializer {
    @Nullable
    private static JsonConfigurationHandler CLIENT_CONFIGS;

    public ClientModInit(){
        super();
        AbstractClientModInitializer.ENDINV_CLIENT = this;
    }

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(KEY_MAPPING_MAP.get(OPEN_MENU));
        KeyBindingHelper.registerKeyBinding(KEY_MAPPING_MAP.get(QUICK_MOVE));//it may be unchangeable
        if(FabricLoader.getInstance().isModLoaded("jei")){
            KeyBindingHelper.registerKeyBinding(KEY_MAPPING_MAP.get(STAR_ITEM));
        }else KeyBindingHelper.registerKeyBinding(KEY_MAPPING_MAP.get(STAR_ITEM_ALTER));
        initClientConfigs();
        FabricClientNetworking.init();
        ClientEvents.register();
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            if (CLIENT_CONFIGS != null) CLIENT_CONFIGS.save();
        });
    }

    @Override
    protected void regKeyParam(KeyMappings.KeyParam key) {
        KeyMapping mapping = new KeyMapping(key.key(), key.type(), key.keyCode(), key.category());
        KEY_MAPPING_MAP.put(key, mapping);
    }

    @Override
    protected IInputHandler getInputHandler() {
        return new IInputHandler() {
            @Override
            public boolean isActiveAndMatches(KeyParam keyParam, InputWithModifiers input) {
                AbstractClientModInitializer modClient = AbstractClientModInitializer.ENDINV_CLIENT;
                if(modClient == null){
                    throw new IllegalStateException("Client mod not initialized");
                }
                if(!keyParam.condition().isActive()) return false;
                if(!keyParam.modifier().matchesModifier(input)) return false;
                //fabric hot fix
                if(input instanceof  MouseButtonEvent buttonEvent
                        && keyParam.keyCode() == buttonEvent.button()
                        && keyParam.modifier().matchesModifier(input)
                ) return true;
                var reg = modClient.KEY_MAPPING_MAP.get(keyParam);
                return switch (input){
                    case KeyEvent keyEvent -> reg.matches(keyEvent);
                    case MouseButtonEvent buttonEvent -> reg.matchesMouse(buttonEvent);
                    default -> false;
                };
            }
        };
    }

    protected void initClientConfigs() {
        CLIENT_CONFIGS = new JsonConfigurationHandler(
                net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve("endless_inventory-client.json"),
                ClientConfigs.getConfigs()
        );
        CLIENT_CONFIGS.load();
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
}
