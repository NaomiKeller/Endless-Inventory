package com.kwwsyk.endinv.neoforge.events;

import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.common.network.payloads.SyncedConfig;
import com.kwwsyk.endinv.common.network.payloads.toClient.*;
import com.kwwsyk.endinv.common.network.payloads.toServer.*;
import com.kwwsyk.endinv.neoforge.network.payloads.JeiAttachedTransferPayload;
import com.kwwsyk.endinv.neoforge.network.payloads.JeiTransferRecipePayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = ModInfo.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class PayloadReg {



    @SubscribeEvent
    public static void registerPayload(final RegisterPayloadHandlersEvent event){
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playBidirectional(
                SyncedConfig.TYPE,
                SyncedConfig.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        (pl,cxt)->pl.handle(cxt::player),
                        (pl,cxt)->pl.handle(cxt::player)
                )
        );
        registrar.playToServer(
                ItemPageContext.TYPE,
                ItemPageContext.STREAM_CODEC,
                (pl,cxt)->pl.handle(cxt::player)
        );
        registrar.playToClient(
                SetItemDisplayContentPayload.TYPE,
                SetItemDisplayContentPayload.STREAM_CODEC,
                (pl,cxt)->pl.handle(cxt::player)
        );
        registrar.playToServer(
                ItemClickPayload.TYPE,
                ItemClickPayload.STREAM_CODEC,
                (pl,cxt)->pl.handle(cxt::player)
        );
        registrar.playToServer(
                ToggleCraftingPayload.TYPE,
                ToggleCraftingPayload.STREAM_CODEC,
                (pl,cxt)->pl.handle(cxt::player)
        );
        registrar.playToClient(
                EndInvMetadata.TYPE,
                EndInvMetadata.STREAM_CODEC,
                (pl,cxt)->pl.handle(cxt::player)
        );
        registrar.playToServer(
                OpenEndInvPayload.TYPE,
                OpenEndInvPayload.STREAM_CODEC,
                (pl,cxt)->pl.handle(cxt::player)
        );
        registrar.playToServer(
                CreativeItemModPayload.TYPE,
                CreativeItemModPayload.STREAM_CODEC,
                (pl,cxt)->pl.handle(cxt::player)
        );
        registrar.playToClient(
                ItemPickedUpPayload.TYPE,
                ItemPickedUpPayload.STREAM_CODEC,
                (pl,cxt)->pl.handle(cxt::player)
        );
        registrar.playToServer(
                StarItemPayload.TYPE,
                StarItemPayload.STREAM_CODEC,
                (pl,cxt)->pl.handle(cxt::player)
        );
        registrar.playToServer(
                QuickMoveToPagePayload.TYPE,
                QuickMoveToPagePayload.STREAM_CODEC,
                (pl,cxt)->pl.handle(cxt::player)
        );
        registrar.playToClient(
                SetStarredPagePayload.TYPE,
                SetStarredPagePayload.STREAM_CODEC,
                (pl,cxt)->pl.handle(cxt::player)
        );
        registrar.playToClient(
                EndInvContent.TYPE,
                EndInvContent.STREAM_CODEC,
                (pl,cxt)->pl.handle(cxt::player)
        );
        registrar.playToServer(
                JeiTransferRecipePayload.TYPE,
                JeiTransferRecipePayload.STREAM_CODEC,
                (pl,cxt)->pl.handle(cxt::player)
        );
        registrar.playToServer(
                JeiAttachedTransferPayload.TYPE,
                JeiAttachedTransferPayload.STREAM_CODEC,
                (pl,cxt)->pl.handle(cxt::player)
        );
        registrar.playToClient(
                MenuAttachabilityPayload.TYPE,
                MenuAttachabilityPayload.STREAM_CODEC,
                (pl, cxt)->pl.handle(cxt::player)
        );
        // Note: This project version does not use StreamCodec here; MenuAttachabilityPayload is handled on Forge/Fabric.
    }
}
