package com.kwwsyk.endinv.common.network.payloads.toClient;

import com.kwwsyk.endinv.common.AbstractModInitializer;
import com.kwwsyk.endinv.common.client.CachedSrcInv;
import com.kwwsyk.endinv.common.client.gui.page.ItemDisplay;
import com.kwwsyk.endinv.common.network.payloads.ModPacketContext;
import com.kwwsyk.endinv.common.network.payloads.ModPacketPayload;
import com.kwwsyk.endinv.common.util.ItemKey;
import com.kwwsyk.endinv.common.util.ItemState;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Map;

public record EndInvContent(Map<ItemKey, ItemState> itemMap) implements ModPacketPayload {

    public static void encodeItemMap(Map<ItemKey,ItemState> map, FriendlyByteBuf o){
        o.writeMap(map,
                (buf, key) -> ItemKey.encode((net.minecraft.network.RegistryFriendlyByteBuf) buf, key),
                (buf, state) -> ItemState.encode((net.minecraft.network.RegistryFriendlyByteBuf) buf, state)
        );
    }

    public static Map<ItemKey,ItemState> decodeItemMap(FriendlyByteBuf o){
        return o.readMap(Object2ObjectLinkedOpenHashMap::new,
                buf -> ItemKey.decode((net.minecraft.network.RegistryFriendlyByteBuf) buf),
                buf -> ItemState.decode((net.minecraft.network.RegistryFriendlyByteBuf) buf)
        );
    }

    public static void encode(EndInvContent content,FriendlyByteBuf o){
        encodeItemMap(content.itemMap,o);
    }

    public static EndInvContent decode(FriendlyByteBuf o){
        return new EndInvContent(decodeItemMap(o));
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, EndInvContent> STREAM_CODEC =
            StreamCodec.of((buf, value) -> encode(value, buf), EndInvContent::decode);

    public static final CustomPacketPayload.Type<EndInvContent> TYPE =
            new CustomPacketPayload.Type<>(AbstractModInitializer.withModLocation("endinv_content"));

    @Override
    public String id() {
        return "endinv_content";
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(ModPacketContext context){
        CachedSrcInv.INSTANCE.initializeContents(this.itemMap());

        ModPacketPayload.getClientPageMeta().ifPresent(
                mng->{
                    if(mng.getDisplayingPage() instanceof ItemDisplay itemPage){
                        itemPage.readCachedItems();//9.25 update: packet invoke page item refresh, item refresh should not send packet (ItemPageContext)
                    }
                }
        );
    }
}
