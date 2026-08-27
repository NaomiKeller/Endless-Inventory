package com.kwwsyk.endinv.common.network.payloads.toServer;

import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.common.ServerLevelEndInv;
import com.kwwsyk.endinv.common.network.payloads.ModPacketContext;
import com.kwwsyk.endinv.common.network.payloads.ModPacketPayload;
import com.kwwsyk.endinv.common.network.payloads.toClient.SetStarredPagePayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record StarItemPayload(ItemStack stack,boolean isAdding) implements ModPacketPayload {

    public static void encode(StarItemPayload payload, FriendlyByteBuf o){
        //FriendlyByteBuf#writeItem encodes count as a single byte, silently wrapping/corrupting
        //anything above 127 (and landing on 0 or negative for plenty of values, which then trips
        //the isEmpty() guards downstream). EndInv stacks routinely exceed vanilla's stack limits,
        //so send identity with a dummy count of 1 and the real count as a separate VarInt.
        o.writeItem(payload.stack.copyWithCount(1));
        o.writeVarInt(payload.stack.getCount());
        o.writeBoolean(payload.isAdding);
    }

    public static StarItemPayload decode(FriendlyByteBuf o){
        ItemStack keyStack = o.readItem();
        int count = o.readVarInt();
        boolean isAdding = o.readBoolean();
        ItemStack stack = keyStack.isEmpty() ? ItemStack.EMPTY : keyStack.copyWithCount(count);
        return new StarItemPayload(stack, isAdding);
    }

    @Override
    public String id() {
        return "star_item";
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
