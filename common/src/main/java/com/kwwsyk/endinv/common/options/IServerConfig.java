package com.kwwsyk.endinv.common.options;

import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.common.network.payloads.SyncedConfig;
import com.kwwsyk.endinv.common.util.Accessibility;

public interface IServerConfig {

    IConfigValue<Integer> getMaxAllowedStackSize();

    IConfigValue<Boolean> allowInfinityMode();

    IConfigValue<Boolean> enableAttaching();

    IConfigValue<Boolean> enableAutoPick();

    default void onAttachingOrAutopickConfigChanged(){
        ModInfo.getPacketDistributor().sendToAllPlayer(new SyncedConfig(enableAttaching().get(),enableAutoPick().get()));
    }

    IConfigValue<ContentTransferMode> transferMode();

    IConfigValue<Accessibility> defaultAccessibility();

    IConfigValue<MissingEndInvPolicy> policyHandlingMissing();

    IConfigValue<Boolean> doConvertEmptyTag();

    IConfigValue<SpecifiedMenuAttachingConfig> specifiedMenuAttachability();
}
