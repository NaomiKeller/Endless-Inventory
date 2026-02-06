package com.kwwsyk.endinv.common.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Objects;

public record ItemKey(Item item, CompoundTag tag) {

    public static final ItemKey EMPTY = new ItemKey(Items.AIR, null);

    public static void encode(FriendlyByteBuf o,ItemKey key){
        o.writeItem(key.toStack(1));
    }

    public static ItemKey decode(FriendlyByteBuf O) {
        ItemStack stack = O.readItem();
        return new ItemKey(stack.getItem(), stack.getTag());
    }

    public boolean isEmpty(){
        return Objects.equals(item, Items.AIR);
    }

    public ItemStack toStack(int count){
        var ret = new ItemStack(item,count);
        ret.setTag(tag);
        return ret;
    }

    public static ItemKey asKey(ItemStack stack){
        return new ItemKey(stack.getItem(),stack.getTag());
    }
}
