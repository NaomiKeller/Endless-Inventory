package com.kwwsyk.endinv.common;

import com.kwwsyk.endinv.common.client.ClientModInfo;
import com.kwwsyk.endinv.common.client.IContainerScreenHelper;
import com.kwwsyk.endinv.common.client.IInputHandler;

public abstract class AbstractClientModInitializer {

    protected AbstractClientModInitializer(){
        ModInfo.clientLoaded = true;
        loadClientConfig();
        ClientModInfo.inputHandler = getInputHandler();
        ClientModInfo.containerScreenHelper = getScreenHelper();
    }

    protected abstract void loadClientConfig();

    protected abstract IInputHandler getInputHandler();

    protected abstract IContainerScreenHelper getScreenHelper();

}
