package com.kwwsyk.endinv.common.util;

import com.kwwsyk.endinv.common.SourceInventory;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;

public enum SortType {
    DEFAULT("sorttype.endinv.default"),
    COUNT("sorttype.endinv.count"),
    SPACE_AND_NAME("sorttype.endinv.name"),
    ID("sorttype.endinv.id"),
    LAST_MODIFIED("sorttype.endinv.last_modified");

    public final String translationKey;

    SortType(String translationKey){
        this.translationKey = translationKey;
    }

    public static final Codec<SortType> CODEC = Codec.STRING.xmap(
            name -> {
                try {
                    return SortType.valueOf(name.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return SortType.DEFAULT;
                }
            },
            SortType::name
    );

    public interface ISortHelper{

        default Comparator<ItemStack> getComparator(SortType sortType, SourceInventory srcInv){
            return switch (sortType){
                case DEFAULT -> (a,b)->0;
                case COUNT -> Comparator.comparingInt(ItemStack::getCount);
                case SPACE_AND_NAME -> Comparator.comparing(it-> BuiltInRegistries.ITEM.getKey(it.getItem()).toString());
                case ID -> REGISTRY_ORDER_COMPARATOR;
                //getItemMap() reads the live map directly; snapshotItemMap() deep-copies the whole
                //inventory, and a Comparator gets invoked O(n log n) times for one sort, so this
                //was copying the entire inventory that many times over for a single sort.
                case LAST_MODIFIED -> Comparator.comparingLong(s -> srcInv.getItemMap().get(ItemKey.asKey(s)).lastModTime());
            };
        }

        Comparator<ItemStack> REGISTRY_ORDER_COMPARATOR = Comparator.comparingInt(
                stack -> BuiltInRegistries.ITEM.getId(stack.getItem())
        );
    }
}
