package com.kwwsyk.endinv.common.network.payloads;

import com.kwwsyk.endinv.common.AbstractModInitializer;
import com.kwwsyk.endinv.common.client.gui.ScreenFramework;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Optional;

public interface ModPacketPayload extends CustomPacketPayload {

    String id();

    default Type<? extends CustomPacketPayload> type(){
        return new Type<>(AbstractModInitializer.withModLocation(id()));
    }

    void handle(ModPacketContext context);

    static Optional<com.kwwsyk.endinv.common.client.gui.page.manager.PageManager> getClientPageMeta(){
        return Optional.ofNullable(ScreenFramework.getInstance());
    }
}
