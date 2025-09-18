package com.kwwsyk.endinv.fabric;

import com.kwwsyk.endinv.common.network.IPacketDistributor;
import com.kwwsyk.endinv.common.network.payloads.ModPacketPayload;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public class FabricPacketDistributor implements IPacketDistributor {

    @Override
    public void sendToServer(ModPacketPayload payload) {
        // Fabric clients send to server via ClientPlayNetworking
        var buf = PacketByteBufs.create();
        buf.writeUtf(payload.id());
        // no serialisation implemented here; rely on existing common handlers if any
        ClientPlayNetworking.send(new net.minecraft.resources.ResourceLocation("endless_inventory","main"), buf);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, ModPacketPayload payload) {
        var buf = PacketByteBufs.create();
        buf.writeUtf(payload.id());
        ServerPlayNetworking.send(player, new net.minecraft.resources.ResourceLocation("endless_inventory","main"), buf);
    }
}
