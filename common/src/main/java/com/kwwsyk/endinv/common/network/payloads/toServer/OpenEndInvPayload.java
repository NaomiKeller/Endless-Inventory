package com.kwwsyk.endinv.common.network.payloads.toServer;

import com.kwwsyk.endinv.common.ModRegistries;
import com.kwwsyk.endinv.common.ServerLevelEndInv;
import com.kwwsyk.endinv.common.menu.EndlessInventoryMenu;
import com.kwwsyk.endinv.common.menu.page.pageManager.AttachingManager;
import com.kwwsyk.endinv.common.network.payloads.ModPacketContext;
import com.kwwsyk.endinv.common.network.payloads.ModPacketPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**Send to server,
 * to open {@link EndlessInventoryMenu} when player is not opening a menu,
 * or to mention server to attach an {@link AttachingManager} if player is opening a menu.
 * @param openNew true for opening a new EIM, false -> ignore rows
 * @param rows The row data to adjust position of EIM widgets
 * @param columns reserved data, default to 9
 */
public record OpenEndInvPayload(boolean openNew, int rows, int columns) implements ModPacketPayload {

    public OpenEndInvPayload(boolean ofMenu, int rows){
        this(ofMenu,rows,9);
    }

    public OpenEndInvPayload(){
        this(false,6);
    }

    public static void encode(OpenEndInvPayload payload, FriendlyByteBuf o){
        o.writeBoolean(payload.openNew);o.writeInt(payload.rows);o.writeInt(payload.columns);
    }

    public static OpenEndInvPayload decode(FriendlyByteBuf o){
        return new OpenEndInvPayload(o.readBoolean(),o.readInt(),o.readInt());
    }

    @Override
    public String id() {
        return "open_endinv";
    }

    @Override
    public void handle(ModPacketContext iPayloadContext) {
        ServerPlayer player = (ServerPlayer) iPayloadContext.player();
        if(player==null) return;
        if(!ModRegistries.NbtAttachments.getSyncedConfig().getWith(player).attaching()) return;
        if(player.containerMenu == player.inventoryMenu && openNew()){
            player.openMenu(EndlessInventoryMenu.provide(rows));
        }else if(!openNew()){
            ServerLevelEndInv.getEndInvForPlayer(player).ifPresent(endInv->{
                AttachingManager manager = new AttachingManager(player.containerMenu, endInv ,player);
                ServerLevelEndInv.PAGE_META_DATA_MANAGER.put(player,manager);
                manager.sendEndInvData();
            });

        }

    }

}
