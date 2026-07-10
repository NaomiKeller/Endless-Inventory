package com.kwwsyk.endinv.neoforge.integrates.irons_spellbooks;

import com.kwwsyk.endinv.common.client.gui.page.ItemEntryDisplay;
import com.kwwsyk.endinv.common.menu.page.PageType;
import com.kwwsyk.endinv.common.menu.page.PageTypeRegistry;
import com.kwwsyk.endinv.common.util.SortType;
import com.kwwsyk.endinv.neoforge.integrates.compat.CompatEntryPage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class IronsSpellbooksPageType {

    private static final List<SortType> SPELL_SORT_TYPES = List.of(
            IronsSpellbooksCompat.SPELL_QUALITY,
            SortType.COUNT,
            SortType.SPACE_AND_NAME,
            SortType.ID,
            SortType.LAST_MODIFIED
    );

    public static final PageType SPELLS_PAGE_TYPE = new PageType(
            (type, meta) -> {
                var ret = new CompatEntryPage(type, meta, (p, s) ->
                        ItemEntryDisplay.DescriptionProvider.ofRowCount(p, AbstractContainerScreen.getTooltipFromItem(Minecraft.getInstance(), s), false, 1), SPELL_SORT_TYPES);
                ret.name = Component.translatableWithFallback("page.endinv." + type.registerName, "Spells Page");
                return ret;
            },
            "spells",
            IronsSpellbooksCompat::isSpellItem,
            ResourceLocation.parse("irons_spellbooks:scroll")
    );

    public static void register(){
        PageTypeRegistry.register(SPELLS_PAGE_TYPE, 0x550);
    }
}
