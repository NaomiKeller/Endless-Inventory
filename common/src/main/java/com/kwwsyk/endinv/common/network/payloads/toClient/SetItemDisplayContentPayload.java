package com.kwwsyk.endinv.common.network.payloads.toClient;

import com.kwwsyk.endinv.common.client.gui.page.ItemDisplay;
import com.kwwsyk.endinv.common.network.payloads.ModPacketContext;
import com.kwwsyk.endinv.common.network.payloads.ModPacketPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**Packet that contains required {@link ItemDisplay}'s content view
 *  when {@code TransferMode==PART}
 * @param stacks
 */
public record SetItemDisplayContentPayload(List<ItemStack> stacks) implements ModPacketPayload {

    public static void encode(SetItemDisplayContentPayload payload, FriendlyByteBuf o){
        o.writeCollection(payload.stacks,FriendlyByteBuf::writeItem);
    }

    public static SetItemDisplayContentPayload decode(FriendlyByteBuf o){
        return new SetItemDisplayContentPayload(o.readList(FriendlyByteBuf::readItem));
    }

    @Override
    public String id() {
        return "itemdisplay_content";
    }

    public void handle(ModPacketContext context) {//todo handle such cache
        ModPacketPayload.getClientPageMeta().ifPresent(mng->{
            if(mng.getDisplayingPage() instanceof ItemDisplay itemDisplay){
                itemDisplay.buildContentsWith(stacks);
            }
        });
    }
}
