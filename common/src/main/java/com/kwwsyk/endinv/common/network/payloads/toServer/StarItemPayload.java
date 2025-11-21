package com.kwwsyk.endinv.common.network.payloads.toServer;

import com.kwwsyk.endinv.common.AbstractModInitializer;
import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.common.ServerLevelEndInv;
import com.kwwsyk.endinv.common.network.payloads.ModPacketContext;
import com.kwwsyk.endinv.common.network.payloads.ModPacketPayload;
import com.kwwsyk.endinv.common.network.payloads.toClient.SetStarredPagePayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;


public record StarItemPayload(ItemStack stack,boolean isAdding) implements ModPacketPayload {

    public static void encode(StarItemPayload payload, RegistryFriendlyByteBuf o){
        ItemStack.OPTIONAL_STREAM_CODEC.encode(o, payload.stack);
        o.writeBoolean(payload.isAdding);
    }

    public static StarItemPayload decode(RegistryFriendlyByteBuf o){
        return new StarItemPayload(ItemStack.OPTIONAL_STREAM_CODEC.decode(o),o.readBoolean());
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, StarItemPayload> STREAM_CODEC =
            StreamCodec.of((buf, value) -> encode(value, buf), StarItemPayload::decode);

    public static final CustomPacketPayload.Type<StarItemPayload> TYPE =
            new CustomPacketPayload.Type<>(AbstractModInitializer.withModLocation("star_item"));

    @Override
    public String id() {
        return "star_item";
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(ModPacketContext iPayloadContext) {
        ServerPlayer player = (ServerPlayer) iPayloadContext.player();
        if(player==null) return;
        ServerLevelEndInv.getEndInvForPlayer(player).ifPresent(endInv->{
            if(isAdding()) {
                endInv.affinities.addStarredItem(stack);
            }else {
                endInv.affinities.removeStarredItem(stack);
            }
            ModInfo.getPacketDistributor().sendToPlayer(player, new SetStarredPagePayload(endInv.getStarredItems()));
            endInv.setChanged();
        });
    }
}
