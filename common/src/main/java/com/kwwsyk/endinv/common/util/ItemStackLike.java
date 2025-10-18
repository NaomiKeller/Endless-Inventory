package com.kwwsyk.endinv.common.util;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

public record ItemStackLike(Item item, int count, @Nullable CustomData customData) {

    public static void encode(RegistryFriendlyByteBuf output, ItemStackLike like) {
        ItemStack.STREAM_CODEC.encode(output, like.toKey());
    }

    public static ItemStackLike decode(RegistryFriendlyByteBuf input) {
        return asKey(ItemStack.STREAM_CODEC.decode(input));
    }

    public static ItemStackLike asKey(ItemStack stack) {
        return new ItemStackLike(stack.getItem(), 0, ItemKey.copyCustomData(stack));
    }

    public static ItemStackLike asKey(ItemStack stack, int count) {
        return new ItemStackLike(stack.getItem(), count, ItemKey.copyCustomData(stack));
    }

    public ItemStack toKey() {
        ItemStack result = new ItemStack(item, count);
        ItemKey.applyCustomData(result, customData);
        return result;
    }
}
