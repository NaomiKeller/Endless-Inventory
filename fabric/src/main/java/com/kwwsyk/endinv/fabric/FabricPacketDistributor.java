package com.kwwsyk.endinv.fabric;

import com.kwwsyk.endinv.common.network.IPacketDistributor;
import com.kwwsyk.endinv.common.network.payloads.ModPacketPayload;
import com.kwwsyk.endinv.fabric.network.FabricClientNetworking;
import com.kwwsyk.endinv.fabric.network.FabricNetworking;
import net.minecraft.server.level.ServerPlayer;

public class FabricPacketDistributor implements IPacketDistributor {

    @Override
    public void sendToServer(ModPacketPayload payload) {
        FabricClientNetworking.sendToServer(payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, ModPacketPayload payload) {
        FabricNetworking.sendToPlayer(player, payload);
    }

    @Override
    public void sendToAllPlayer(ModPacketPayload payload) {

    }
}
