package com.kwwsyk.endinv.common;

import com.kwwsyk.endinv.common.client.ClientModInfo;
import com.kwwsyk.endinv.common.client.IContainerScreenHelper;
import com.kwwsyk.endinv.common.client.IInputHandler;

public abstract class AbstractClientModInitializer {

    protected AbstractClientModInitializer(){
        ModInfo.clientLoaded = true;
        initClientConfigs();
        ClientModInfo.inputHandler = getInputHandler();
        ClientModInfo.containerScreenHelper = getScreenHelper();
    }

    protected abstract void initClientConfigs();

    protected abstract IInputHandler getInputHandler();

    protected abstract IContainerScreenHelper getScreenHelper();

}
