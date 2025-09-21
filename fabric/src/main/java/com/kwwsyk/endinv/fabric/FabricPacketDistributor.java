package com.kwwsyk.endinv.fabric;

import com.kwwsyk.endinv.common.network.IPacketDistributor;
import com.kwwsyk.endinv.common.network.payloads.ModPacketPayload;
import com.kwwsyk.endinv.fabric.network.FabricNetworking;
import net.minecraft.server.level.ServerPlayer;

public class FabricPacketDistributor implements IPacketDistributor {

    @Override
    public void sendToServer(ModPacketPayload payload) {
        FabricNetworking.sendToServer(payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, ModPacketPayload payload) {
        FabricNetworking.sendToPlayer(player, payload);
    }
}
