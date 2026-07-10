package com.kwwsyk.endinv.neoforge.integrates.irons_spellbooks;

import com.kwwsyk.endinv.common.util.SortType;
import io.redspace.ironsspellbooks.api.item.IScroll;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellSlot;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.Comparator;

public final class IronsSpellbooksCompat {

    private static final Comparator<ItemStack> REGISTRY_NAME_COMPARATOR =
            Comparator.comparing(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());

    public static final SortType SPELL_QUALITY = SortType.register(new SortType(
            "SPELL_QUALITY",
            "sorttype.endinv.spell_quality",
            (srcInv, sortType) -> Comparator.comparingDouble(IronsSpellbooksCompat::spellQuality)
                    .thenComparing(REGISTRY_NAME_COMPARATOR)
    ));

    private IronsSpellbooksCompat() {
    }

    public static boolean isSpellItem(ItemStack stack) {
        return stack.getItem() instanceof IScroll;
    }

    private static double spellQuality(ItemStack stack) {
        if(!ISpellContainer.isSpellContainer(stack)) return stack.getRarity().ordinal();
        ISpellContainer container = ISpellContainer.get(stack);
        if(container == null || container.isEmpty()) return stack.getRarity().ordinal();
        return Arrays.stream(container.getAllSpells())
                .map(SpellSlot::spellData)
                .filter(data -> data != null && !data.equals(io.redspace.ironsspellbooks.api.spells.SpellData.EMPTY))
                .mapToInt(data -> data.getRarity().getValue())
                .max()
                .orElse(stack.getRarity().ordinal());
    }
}
