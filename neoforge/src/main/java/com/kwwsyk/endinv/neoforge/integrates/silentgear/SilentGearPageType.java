package com.kwwsyk.endinv.neoforge.integrates.silentgear;

import com.kwwsyk.endinv.common.menu.page.PageType;
import com.kwwsyk.endinv.common.menu.page.PageTypeRegistry;
import com.kwwsyk.endinv.common.util.SortType;
import com.kwwsyk.endinv.neoforge.integrates.compat.CompatItemPage;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class SilentGearPageType {

    private static final List<SortType> MATERIAL_SORT_TYPES = List.of(
            SilentGearCompat.GEAR_MATERIAL_DURABILITY,
            SilentGearCompat.GEAR_MATERIAL_HARVEST_SPEED,
            SilentGearCompat.GEAR_MATERIAL_MELEE_DAMAGE,
            SilentGearCompat.GEAR_MATERIAL_MAGIC_DAMAGE,
            SilentGearCompat.GEAR_MATERIAL_RANGED_DAMAGE,
            SilentGearCompat.GEAR_MATERIAL_DRAW_SPEED,
            SilentGearCompat.GEAR_MATERIAL_PROJECTILE_SPEED,
            SilentGearCompat.GEAR_MATERIAL_ARMOR,
            SilentGearCompat.GEAR_MATERIAL_ARMOR_TOUGHNESS,
            SilentGearCompat.GEAR_MATERIAL_ENCHANTMENT_VALUE,
            SortType.COUNT,
            SortType.SPACE_AND_NAME,
            SortType.ID,
            SortType.LAST_MODIFIED
    );

    public static final PageType MATERIALS_PAGE_TYPE = new PageType(
            (type, meta) -> {
                var ret = new CompatItemPage(type, meta, MATERIAL_SORT_TYPES);
                ret.name = Component.translatableWithFallback("page.endinv." + type.registerName, "Silent Gear Materials");
                return ret;
            },
            "silent_gear_materials",
            SilentGearCompat::isMaterial,
            ResourceLocation.withDefaultNamespace("iron_ingot")
    );

    public static void register(){
        PageTypeRegistry.register(MATERIALS_PAGE_TYPE, 0x560);
    }
}
