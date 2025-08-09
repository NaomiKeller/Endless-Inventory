package com.kwwsyk.endinv.common.network.payloads.toServer;

import com.kwwsyk.endinv.common.ServerLevelEndInv;
import com.kwwsyk.endinv.common.menu.page.pageManager.PageMetaDataManager;
import com.kwwsyk.endinv.common.network.payloads.ModPacketContext;
import com.kwwsyk.endinv.common.network.payloads.ModPacketPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Used when client item modified in ItemDisplay with {@link net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.ItemPickerMenu}
 * @param isAdding true for add item and false for take item.
 */
public record CreativeItemModPayload(ItemStack stack, boolean isAdding) implements ModPacketPayload {

    public static void encode(CreativeItemModPayload payload, FriendlyByteBuf o){
        o.writeItem(payload.stack);
        o.writeBoolean(payload.isAdding);
    }

    public static CreativeItemModPayload decode(FriendlyByteBuf o){
        return new CreativeItemModPayload(o.readItem(),o.readBoolean());
    }


    @Override
    public String id() {
        return "item_modify";
    }

    @Override
    public void handle(ModPacketContext iPayloadContext) {
        ServerPlayer player = (ServerPlayer) iPayloadContext.player();
        if(player==null)return;
        if(player.containerMenu.getCarried().isEmpty() && player.isCreative()){
            var optional = ServerLevelEndInv.checkAndGetManagerForPlayer(player);
            if(optional.isEmpty()) return;
            PageMetaDataManager manager = optional.get();
            if(isAdding){
                manager.getSourceInventory().addItem(stack);
            }else {
                ItemStack taken = manager.getSourceInventory().takeItem(stack);
                if(!taken.isEmpty()) player.containerMenu.setCarried(taken);
            }
        }
    }
}
