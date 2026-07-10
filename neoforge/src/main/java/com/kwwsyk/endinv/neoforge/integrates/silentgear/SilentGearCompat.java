package com.kwwsyk.endinv.neoforge.integrates.silentgear;

import com.kwwsyk.endinv.common.util.SortType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.silentchaos512.gear.api.part.PartType;
import net.silentchaos512.gear.api.property.NumberProperty;
import net.silentchaos512.gear.gear.material.MaterialInstance;
import net.silentchaos512.gear.setup.gear.GearProperties;
import net.silentchaos512.gear.setup.gear.PartTypes;

import java.util.Collection;
import java.util.Comparator;
import java.util.function.Supplier;

public final class SilentGearCompat {

    private static final Comparator<ItemStack> REGISTRY_NAME_COMPARATOR =
            Comparator.comparing(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());

    public static final SortType GEAR_MATERIAL_DURABILITY = materialSort("DURABILITY", "sorttype.endinv.gear_material_durability", GearProperties.DURABILITY);
    public static final SortType GEAR_MATERIAL_HARVEST_SPEED = materialSort("HARVEST_SPEED", "sorttype.endinv.gear_material_harvest_speed", GearProperties.HARVEST_SPEED);
    public static final SortType GEAR_MATERIAL_MELEE_DAMAGE = materialSort("MELEE_DAMAGE", "sorttype.endinv.gear_material_melee_damage", GearProperties.ATTACK_DAMAGE);
    public static final SortType GEAR_MATERIAL_MAGIC_DAMAGE = materialSort("MAGIC_DAMAGE", "sorttype.endinv.gear_material_magic_damage", GearProperties.MAGIC_DAMAGE);
    public static final SortType GEAR_MATERIAL_RANGED_DAMAGE = materialSort("RANGED_DAMAGE", "sorttype.endinv.gear_material_ranged_damage", GearProperties.RANGED_DAMAGE);
    public static final SortType GEAR_MATERIAL_DRAW_SPEED = materialSort("DRAW_SPEED", "sorttype.endinv.gear_material_draw_speed", GearProperties.DRAW_SPEED);
    public static final SortType GEAR_MATERIAL_PROJECTILE_SPEED = materialSort("PROJECTILE_SPEED", "sorttype.endinv.gear_material_projectile_speed", GearProperties.PROJECTILE_SPEED);
    public static final SortType GEAR_MATERIAL_ARMOR = materialSort("ARMOR", "sorttype.endinv.gear_material_armor", GearProperties.ARMOR);
    public static final SortType GEAR_MATERIAL_ARMOR_TOUGHNESS = materialSort("ARMOR_TOUGHNESS", "sorttype.endinv.gear_material_armor_toughness", GearProperties.ARMOR_TOUGHNESS);
    public static final SortType GEAR_MATERIAL_ENCHANTMENT_VALUE = materialSort("ENCHANTMENT_VALUE", "sorttype.endinv.gear_material_enchantment_value", GearProperties.ENCHANTMENT_VALUE);

    private SilentGearCompat() {
    }

    public static boolean isMaterial(ItemStack stack) {
        MaterialInstance materialInstance = MaterialInstance.from(stack);
        return materialInstance != null && materialInstance.isValid();
    }

    private static SortType materialSort(String name, String translationKey, Supplier<NumberProperty> property) {
        return SortType.register(new SortType(
                name,
                translationKey,
                (srcInv, sortType) -> Comparator.comparingDouble((ItemStack stack) -> materialValue(stack, property.get()))
                        .thenComparing(REGISTRY_NAME_COMPARATOR)
        ));
    }

    private static double materialValue(ItemStack stack, NumberProperty property) {
        MaterialInstance material = MaterialInstance.from(stack);
        if(material == null || !material.isValid()) return Double.NEGATIVE_INFINITY;
        Collection<PartType> partTypes = material.getPartTypes();
        if(partTypes.isEmpty()) return material.getProperty(PartTypes.MAIN.get(), property);
        return partTypes.stream()
                .mapToDouble(partType -> material.getProperty(partType, property))
                .max()
                .orElse(Double.NEGATIVE_INFINITY);
    }
}
