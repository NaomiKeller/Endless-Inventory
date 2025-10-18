package com.kwwsyk.endinv.common.network.payloads.toClient;

import com.kwwsyk.endinv.common.AbstractModInitializer;
import com.kwwsyk.endinv.common.client.gui.page.ItemDisplay;
import com.kwwsyk.endinv.common.network.payloads.ModPacketContext;
import com.kwwsyk.endinv.common.network.payloads.ModPacketPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**Packet that contains required {@link ItemDisplay}'s content view
 *  when {@code TransferMode==PART}
 * @param stacks
 */
public record SetItemDisplayContentPayload(List<ItemStack> stacks) implements ModPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, SetItemDisplayContentPayload> STREAM_CODEC =
            StreamCodec.of((buf, value) -> encode(value, buf), SetItemDisplayContentPayload::decode);

    public static final CustomPacketPayload.Type<SetItemDisplayContentPayload> TYPE =
            new CustomPacketPayload.Type<>(AbstractModInitializer.withModLocation("itemdisplay_content"));

    public static void encode(SetItemDisplayContentPayload payload, FriendlyByteBuf o){
        o.writeCollection(payload.stacks,(buf, stack) -> net.minecraft.world.item.ItemStack.STREAM_CODEC.encode((net.minecraft.network.RegistryFriendlyByteBuf) buf, stack));
    }

    public static SetItemDisplayContentPayload decode(FriendlyByteBuf o){
        return new SetItemDisplayContentPayload(o.readList(buf -> net.minecraft.world.item.ItemStack.STREAM_CODEC.decode((net.minecraft.network.RegistryFriendlyByteBuf) buf)));
    }

    @Override
    public String id() {
        return "itemdisplay_content";
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(ModPacketContext context) {//todo handle such cache
        ModPacketPayload.getClientPageMeta().ifPresent(mng->{
            if(mng.getDisplayingPage() instanceof ItemDisplay itemDisplay){
                itemDisplay.buildContentsWith(stacks);
            }
        });
    }
}
