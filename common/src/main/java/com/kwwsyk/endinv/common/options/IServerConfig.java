package com.kwwsyk.endinv.common.options;

import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.common.network.payloads.SyncedConfig;
import com.kwwsyk.endinv.common.util.Accessibility;

public interface IServerConfig {

    LegacyIConfigValue<Integer> getMaxAllowedStackSize();

    LegacyIConfigValue<Boolean> allowInfinityMode();

    LegacyIConfigValue<Boolean> enableAttaching();

    LegacyIConfigValue<Boolean> enableAutoPick();

    default void onAttachingOrAutopickConfigChanged(){
        ModInfo.getPacketDistributor().sendToAllPlayer(new SyncedConfig(enableAttaching().get(),enableAutoPick().get()));
    }

    /**
     * Notify all players that the server's specified menu attachability has changed.
     * Broadcasts an effective attachability snapshot for client-side checks.
     */
    default void onSpecifiedMenuAttachabilityChanged(){
        var config = specifiedMenuAttachability().get();
        boolean defaultAttach = enableAttaching().get();
        var payload = new com.kwwsyk.endinv.common.network.payloads.toClient.MenuAttachabilityPayload(
                defaultAttach,
                config.isInventoryAttachable(),
                config.getConfigs()
        );
        ModInfo.getPacketDistributor().sendToAllPlayer(payload);
    }

    LegacyIConfigValue<ContentTransferMode> transferMode();

    LegacyIConfigValue<Accessibility> defaultAccessibility();

    LegacyIConfigValue<MissingEndInvPolicy> policyHandlingMissing();

    LegacyIConfigValue<Boolean> doConvertEmptyTag();

    LegacyIConfigValue<SpecifiedMenuAttachingConfig> specifiedMenuAttachability();
}
