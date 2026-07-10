package com.kwwsyk.endinv.common.util;

import com.kwwsyk.endinv.common.SourceInventory;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.function.BiFunction;

public final class SortType {

    private static final Map<String, SortType> REGISTRY = new LinkedHashMap<>();

    public static final Comparator<ItemStack> REGISTRY_ORDER_COMPARATOR = Comparator.comparingInt(
            stack -> BuiltInRegistries.ITEM.getId(stack.getItem())
    );

    public static final SortType DEFAULT = register(new SortType(
            "DEFAULT",
            "sorttype.endinv.default",
            (srcInv, sortType) -> (a, b) -> 0
    ));
    public static final SortType COUNT = register(new SortType(
            "COUNT",
            "sorttype.endinv.count",
            (srcInv, sortType) -> Comparator.comparingInt(ItemStack::getCount)
    ));
    public static final SortType SPACE_AND_NAME = register(new SortType(
            "SPACE_AND_NAME",
            "sorttype.endinv.name",
            (srcInv, sortType) -> Comparator.comparing(it -> BuiltInRegistries.ITEM.getKey(it.getItem()).toString())
    ));
    public static final SortType ID = register(new SortType(
            "ID",
            "sorttype.endinv.id",
            (srcInv, sortType) -> REGISTRY_ORDER_COMPARATOR
    ));
    public static final SortType LAST_MODIFIED = register(new SortType(
            "LAST_MODIFIED",
            "sorttype.endinv.last_modified",
            (srcInv, sortType) -> Comparator.comparingLong(s -> srcInv.getItemMap().get(ItemKey.asKey(s)).lastModTime())
    ));

    public static final List<SortType> DEFAULT_SORT_TYPES = List.of(
            DEFAULT,
            COUNT,
            SPACE_AND_NAME,
            ID,
            LAST_MODIFIED
    );

    public static final Codec<SortType> CODEC = Codec.STRING.xmap(SortType::byName, SortType::name);

    public final String translationKey;
    private final String name;
    private final BiFunction<SourceInventory, SortType, Comparator<ItemStack>> comparatorFactory;

    public SortType(String name, String translationKey, BiFunction<SourceInventory, SortType, Comparator<ItemStack>> comparatorFactory) {
        this.name = name;
        this.translationKey = translationKey;
        this.comparatorFactory = comparatorFactory;
    }

    public Comparator<ItemStack> getComparator(SourceInventory srcInv) {
        return comparatorFactory.apply(srcInv, this);
    }

    public String name() {
        return name;
    }

    public static SortType register(SortType type) {
        SortType existing = REGISTRY.putIfAbsent(type.name(), type);
        if(existing != null && existing != type) throw new IllegalArgumentException("Duplicate sort type: " + type.name());
        return type;
    }

    public static SortType byName(String name) {
        return REGISTRY.getOrDefault(name, DEFAULT);
    }

    public static SortType valueOf(String name) {
        SortType type = REGISTRY.get(name);
        if(type == null) throw new IllegalArgumentException("Unknown sort type: " + name);
        return type;
    }

    public static List<SortType> values() {
        return List.copyOf(REGISTRY.values());
    }

    public interface ISortHelper{

        default Comparator<ItemStack> getComparator(SortType sortType, SourceInventory srcInv){
            return sortType.getComparator(srcInv);
        }
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SortType sortType && Objects.equals(name, sortType.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
