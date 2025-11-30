package com.kwwsyk.endinv.common.options;

import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.common.network.payloads.SyncedConfig;
import com.kwwsyk.endinv.common.options.config.IConfigValue;
import com.kwwsyk.endinv.common.util.Accessibility;
@Deprecated
public interface IServerConfig {

    IConfigValue<Integer> getMaxAllowedStackSize();//

    IConfigValue<Boolean> allowInfinityMode();//

    IConfigValue<Boolean> enableAttaching();//

    IConfigValue<Boolean> enableAutoPick();//todo

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

    IConfigValue<ContentTransferMode> transferMode();//

    IConfigValue<Accessibility> defaultAccessibility();//

    IConfigValue<CreationEndinvStrategy> policyHandlingMissing();//

    /**itemstack's component is never null
     * @see net.minecraft.world.item.ItemStack
     * @deprecated needn't to impl
     * @since 1.21.4
     */
    @Deprecated
    IConfigValue<Boolean> doConvertEmptyTag();//

    IConfigValue<SpecifiedMenuAttachingConfig> specifiedMenuAttachability();
}
