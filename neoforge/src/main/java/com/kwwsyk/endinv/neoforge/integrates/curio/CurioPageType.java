package com.kwwsyk.endinv.neoforge.integrates.curio;

import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.common.client.gui.page.ItemDisplay;
import com.kwwsyk.endinv.common.menu.page.PageType;
import com.kwwsyk.endinv.common.menu.page.PageTypeRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class CurioPageType {

    public static final PageType CURIO_PAGE_TYPE = new PageType(
            (type,meta)->{
                var ret = new ItemDisplay(type,meta);
                ret.name = Component.translatableWithFallback("page.endinv."+type.registerName, "Curios Page");
                return ret;
            },
            "curios",
            (stack)-> false,
            Identifier.fromNamespaceAndPath(ModInfo.MOD_ID,"textures/curios_icon.png")
    );
    public static final String CURIOS_MODID = "curios";

    public static void register(){
        PageTypeRegistry.register(CURIO_PAGE_TYPE,0x450);
    }
}
