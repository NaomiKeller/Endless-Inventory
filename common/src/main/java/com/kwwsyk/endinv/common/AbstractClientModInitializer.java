package com.kwwsyk.endinv.common;

import com.kwwsyk.endinv.common.client.ClientModInfo;
import com.kwwsyk.endinv.common.client.IContainerScreenHelper;
import com.kwwsyk.endinv.common.client.IInputHandler;
import com.kwwsyk.endinv.common.client.KeyMappings;
import net.minecraft.client.KeyMapping;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static com.kwwsyk.endinv.common.client.KeyMappings.*;

public abstract class AbstractClientModInitializer {
    @Nullable
    public static AbstractClientModInitializer ENDINV_CLIENT;
    public final Map<KeyMappings.KeyParam, KeyMapping> KEY_MAPPING_MAP = new HashMap<>();

    protected AbstractClientModInitializer(){
        ModInfo.clientLoaded = true;
        ClientModInfo.inputHandler = getInputHandler();
        ClientModInfo.containerScreenHelper = getScreenHelper();

        regKeyParam(OPEN_MENU);
        regKeyParam(QUICK_MOVE);
        regKeyParam(STAR_ITEM);
        regKeyParam(STAR_ITEM_ALTER);
    }

    protected abstract void regKeyParam(KeyMappings.KeyParam key);

    protected IInputHandler getInputHandler(){
        return new IInputHandler() {};
    }

    protected abstract IContainerScreenHelper getScreenHelper();

}
