package com.kwwsyk.endinv.common.util;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 *
 * @param item
 * @param components should not be null, but somewhere may construct it to null
 */
public record ItemKey(Item item, DataComponentPatch components) {

    public static final ItemKey EMPTY = new ItemKey(Items.AIR, DataComponentPatch.EMPTY);

    public static final StreamCodec<RegistryFriendlyByteBuf,ItemKey> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(Registries.ITEM),ItemKey::item,
            DataComponentPatch.STREAM_CODEC,ItemKey::components,
            ItemKey::new
    );

    public ItemKey(Item item, @Nullable DataComponentPatch components){
        this.item = item;
        this.components = components==null? DataComponentPatch.EMPTY : components;
    }

    public boolean isEmpty(){
        return Objects.equals(item, Items.AIR);
    }

    public ItemStack toStack(int count){
        return new ItemStack(BuiltInRegistries.ITEM.wrapAsHolder(item),count,components);
    }

    public static ItemKey asKey(ItemStack stack){
        return new ItemKey(stack.getItem(),stack.getComponentsPatch());
    }
}
