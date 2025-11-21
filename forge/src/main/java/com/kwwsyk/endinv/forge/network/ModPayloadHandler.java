package com.kwwsyk.endinv.forge.network;

import com.kwwsyk.endinv.common.AbstractModInitializer;
import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.common.network.payloads.ModPacketContext;
import com.kwwsyk.endinv.common.network.payloads.SyncedConfig;
import com.kwwsyk.endinv.common.network.payloads.toClient.*;
import com.kwwsyk.endinv.common.network.payloads.toServer.*;
import com.kwwsyk.endinv.forge.ModInitializer;
import com.kwwsyk.endinv.forge.network.payloads.JeiAttachedTransferPayload;
import com.kwwsyk.endinv.forge.network.payloads.JeiTransferRecipePayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;

import java.util.function.BiConsumer;

@Mod.EventBusSubscriber(modid = ModInfo.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModPayloadHandler {

    private static final ResourceLocation id = ModInitializer.withModLocation("channel");

    private static Channel<CustomPacketPayload> channel;

    //These are new way on forge to register payloads.
    private static void regChannel(){
        var channel0 = ChannelBuilder.named(AbstractModInitializer.withModLocation("channel"))
                .networkProtocolVersion(1)
                .optional()
                .payloadChannel()
                .play();
        var channelB = channel0.bidirectional()
                .add(SyncedConfig.TYPE, SyncedConfig.STREAM_CODEC, convertBi(SyncedConfig::handle));
        var channelC = channelB.clientbound()
                .add(EndInvMetadata.TYPE, EndInvMetadata.STREAM_CODEC, convertClient(EndInvMetadata::handle))
                .add(ItemPickedUpPayload.TYPE, ItemPickedUpPayload.STREAM_CODEC, convertClient(ItemPickedUpPayload::handle))
                .add(SetItemDisplayContentPayload.TYPE, SetItemDisplayContentPayload.STREAM_CODEC, convertClient(SetItemDisplayContentPayload::handle))
                .add(SetStarredPagePayload.TYPE, SetStarredPagePayload.STREAM_CODEC, convertClient(SetStarredPagePayload::handle))
                .add(EndInvContent.TYPE, EndInvContent.STREAM_CODEC, convertClient(EndInvContent::handle))
                .add(MenuAttachabilityPayload.TYPE, MenuAttachabilityPayload.STREAM_CODEC, convertClient(MenuAttachabilityPayload::handle));
        var channelS = channelC.serverbound()
                .add(ItemClickPayload.TYPE, ItemClickPayload.STREAM_CODEC, convert(ItemClickPayload::handle))
                .add(CreativeItemModPayload.TYPE, CreativeItemModPayload.STREAM_CODEC, convert(CreativeItemModPayload::handle))
                .add(ItemPageContext.TYPE, ItemPageContext.STREAM_CODEC, convert(ItemPageContext::handle))
                .add(OpenEndInvPayload.TYPE, OpenEndInvPayload.STREAM_CODEC, convert(OpenEndInvPayload::handle))
                .add(QuickMoveToPagePayload.TYPE, QuickMoveToPagePayload.STREAM_CODEC, convert(QuickMoveToPagePayload::handle))
                .add(StarItemPayload.TYPE, StarItemPayload.STREAM_CODEC, convert(StarItemPayload::handle))
                .add(ToggleCraftingPayload.TYPE, ToggleCraftingPayload.STREAM_CODEC, convert(ToggleCraftingPayload::handle));
        if(ModList.get().isLoaded("jei")){
            channelS.add(JeiTransferRecipePayload.TYPE,JeiTransferRecipePayload.STREAM_CODEC,convert(JeiTransferRecipePayload::handle))
            .add(JeiAttachedTransferPayload.TYPE,JeiAttachedTransferPayload.STREAM_CODEC,convert(JeiAttachedTransferPayload::handle));
        }
        channel = channelS.build();
    }

    public static <MSG> BiConsumer<MSG, CustomPayloadEvent.Context> convert(BiConsumer<MSG, ModPacketContext> handler){
        return (msg,sup)-> {
            sup.enqueueWork(() -> {
                // Work that needs to be thread-safe (most work)
                // Do stuff
                handler.accept(msg, sup::getSender);
            });
            sup.setPacketHandled(true);
        };
    }

    public static <MSG> BiConsumer<MSG, CustomPayloadEvent.Context> convertClient(BiConsumer<MSG, ModPacketContext> handler){
        return (msg,sup)-> {
            sup.enqueueWork(() -> {
                // Make sure it's only executed on the physical client
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handler.accept(msg, sup::getSender));

            });
            sup.setPacketHandled(true);
        };
    }

    public static <MSG> BiConsumer<MSG, CustomPayloadEvent.Context> convertBi(BiConsumer<MSG, ModPacketContext> handler){
        return (msg, cxt)-> {
            cxt.enqueueWork(() -> {
                ServerPlayer sender;
                if((sender=cxt.getSender())!=null){
                    handler.accept(msg,()->sender);
                }else {
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handler.accept(msg, cxt::getSender));
                }
            });
            cxt.setPacketHandled(true);
        };
    }
    // old message registration removed in favor of regChannel()

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        // register channel during common setup
        event.enqueueWork(ModPayloadHandler::regChannel);
    }

    public static Channel<CustomPacketPayload> getChannel() {
        return channel;
    }
}