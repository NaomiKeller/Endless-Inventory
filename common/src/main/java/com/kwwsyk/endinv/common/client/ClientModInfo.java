package com.kwwsyk.endinv.common.client;

import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.common.client.option.CachedConfig;
import com.kwwsyk.endinv.common.client.option.IClientConfig;
import com.kwwsyk.endinv.common.network.payloads.toServer.OpenEndInvPayload;

public class ClientModInfo {

    private static IClientConfig clientConfig;

    public static IInputHandler inputHandler;

    public static IContainerScreenHelper containerScreenHelper;

    public static IClientConfig getClientConfig() {
        return clientConfig;
    }

    public static void setClientConfig(IClientConfig clientConfig) {
        if(ClientModInfo.clientConfig!=null) throw new IllegalStateException("Try to set config when config has been initialized.");
        ClientModInfo.clientConfig = clientConfig;
    }

    public static void sendOpenMenu(){
        CachedConfig.readAndSyncClientConfigToServer(true);
        ModInfo.getPacketDistributor().sendToServer(new OpenEndInvPayload(true,CachedConfig.currentLayout().rows()));
    }
}
