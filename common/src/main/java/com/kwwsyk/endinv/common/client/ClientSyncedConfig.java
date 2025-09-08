package com.kwwsyk.endinv.common.client;

import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.common.ModRegistries;
import com.kwwsyk.endinv.common.client.option.IClientConfig;
import com.kwwsyk.endinv.common.network.payloads.SyncedConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;

public final class ClientSyncedConfig {

    private ClientSyncedConfig() {}

    public static void readAndSyncClientConfigToServer(boolean ofMenu){
        if(Minecraft.getInstance().player != null){
            SyncedConfig config = readClientConfig(ofMenu);
            updateSyncedConfig(config);
        }
    }

    public static void updateSyncedConfig(SyncedConfig config){
        IClientConfig clientConfig = ClientModInfo.getClientConfig();
        LocalPlayer player;
        if((player = Minecraft.getInstance().player)!=null){
            int rows = clientConfig.rows().get();
            int syncedRows = config.pageData().rows();
            if(rows==0){
                rows = syncedRows;
            }
            int columns = clientConfig.columns().get();
            if(columns==0){
                columns = 9;
            }
            if(Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> screen && clientConfig.autoSuitColumn().get()){
                columns = Math.min(columns,clientConfig.calculateSuitInColumnCount(screen));
            }
            SyncedConfig config1 = new SyncedConfig(config.pageData().ofRowChanged(rows).ofColumnChanged(columns), config.attaching(), config.autoPicking());
            ModRegistries.NbtAttachments.getSyncedConfig().setTo(player,config1);
            ModInfo.getPacketDistributor().sendToServer(config1);
        }
    }

    public static SyncedConfig readClientConfig(boolean ofMenu){
        IClientConfig clientConfig = ClientModInfo.getClientConfig();
        LocalPlayer player;
        if((player=Minecraft.getInstance().player)!=null){
            int rows = clientConfig.rows().get();
            if(rows==0){
                rows = clientConfig.calculateDefaultRowCount(ofMenu);
            }
            int columns = clientConfig.columns().get();
            if(columns==0){
                columns = 9;
            }
            if(Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> screen && clientConfig.autoSuitColumn().get()){
                columns = Math.min(columns,clientConfig.calculateSuitInColumnCount(screen));
            }
            SyncedConfig config = ModRegistries.NbtAttachments.getSyncedConfig().getWith(player);
            return new SyncedConfig(config.pageData().ofRowChanged(rows).ofColumnChanged(columns), clientConfig.attaching().get(), true);
        }else throw new IllegalStateException("Unable to read client config, as running on server or player is not existing.");
    }
}
