package com.kwwsyk.endinv.common.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record ItemStackLike(Item item, int count, CompoundTag tag) {

    public static void encode(FriendlyByteBuf o,ItemStackLike item){
        //write item+tag with a dummy count of 1 so FriendlyByteBuf#writeItem never treats a
        //legitimately zero-count entry (e.g. a starred item currently not held) as an empty stack
        //and silently drops the item identity.
        ItemStack keyStack = new ItemStack(item.item(),1);
        keyStack.setTag(item.tag());
        o.writeItem(keyStack);
        o.writeVarInt(item.count());
    }

    public static ItemStackLike decode(FriendlyByteBuf o){
        ItemStack stack = o.readItem();
        int count = o.readVarInt();
        return new ItemStackLike(stack.getItem(), count, stack.getTag());
    }

    public static ItemStackLike asKey(ItemStack stack){
        return new ItemStackLike(stack.getItem(),0,stack.getTag());
    }

    public static ItemStackLike asKey(ItemStack stack, int count){
        return new ItemStackLike(stack.getItem(),count,stack.getTag());
    }

    public ItemStack toKey(){
        var ret = new ItemStack(item,count);
        ret.setTag(tag);
        return ret;
    }
}
