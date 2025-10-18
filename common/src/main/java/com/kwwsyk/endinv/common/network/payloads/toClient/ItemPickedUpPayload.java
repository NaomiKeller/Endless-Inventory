package com.kwwsyk.endinv.common.network.payloads.toClient;

import com.kwwsyk.endinv.common.AbstractModInitializer;
import com.kwwsyk.endinv.common.client.event.AutoPickTipper;
import com.kwwsyk.endinv.common.network.payloads.ModPacketContext;
import com.kwwsyk.endinv.common.network.payloads.ModPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

public record ItemPickedUpPayload(ItemStack stack) implements ModPacketPayload {
    
    public static void encode(ItemPickedUpPayload payload, RegistryFriendlyByteBuf o){
        ItemStack.STREAM_CODEC.encode(o, payload.stack);
    }
    
    public static ItemPickedUpPayload decode(RegistryFriendlyByteBuf o){
        return new ItemPickedUpPayload(ItemStack.STREAM_CODEC.decode(o));
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemPickedUpPayload> STREAM_CODEC =
            StreamCodec.of((buf, value) -> encode(value, buf), ItemPickedUpPayload::decode);

    public static final CustomPacketPayload.Type<ItemPickedUpPayload> TYPE =
            new CustomPacketPayload.Type<>(AbstractModInitializer.withModLocation("auto_picked"));

    @Override
    public String id() {
        return "auto_picked";
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(ModPacketContext iPayloadContext) {
        AutoPickTipper.addItem(stack());
    }
}
