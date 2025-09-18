package com.kwwsyk.endinv.fabric;

import com.kwwsyk.endinv.common.options.ContentTransferMode;
import com.kwwsyk.endinv.common.options.IConfigValue;
import com.kwwsyk.endinv.common.options.IServerConfig;
import com.kwwsyk.endinv.common.options.MissingEndInvPolicy;
import com.kwwsyk.endinv.common.util.Accessibility;

public class ServerConfig implements IServerConfig {

    // Simple in-memory defaults; Fabric does not provide built-in config like Forge in this project.
    private int maxStack = Integer.MAX_VALUE;
    private boolean enableInfinite = false;
    private boolean enableAutoPick = false;
    private ContentTransferMode transferMode = ContentTransferMode.ALL;
    private Accessibility defaultAccessibility = Accessibility.PUBLIC;
    private MissingEndInvPolicy creationMode = MissingEndInvPolicy.CREATE_PER_PLAYER;
    private boolean convertEmptyTag = true;

    @Override
    public IConfigValue<Integer> getMaxAllowedStackSize() {
        return IConfigValue.of(() -> maxStack, v -> this.maxStack = v);
    }

    @Override
    public IConfigValue<Boolean> allowInfinityMode() {
        return IConfigValue.of(() -> enableInfinite, v -> this.enableInfinite = v);
    }

    @Override
    public IConfigValue<Boolean> enableAutoPick() {
        return IConfigValue.of(() -> enableAutoPick, v -> this.enableAutoPick = v);
    }

    @Override
    public IConfigValue<ContentTransferMode> transferMode() {
        return IConfigValue.of(() -> transferMode, v -> this.transferMode = v);
    }

    @Override
    public IConfigValue<Accessibility> defaultAccessibility() {
        return IConfigValue.of(() -> defaultAccessibility, v -> this.defaultAccessibility = v);
    }

    @Override
    public IConfigValue<MissingEndInvPolicy> policyHandlingMissing() {
        return IConfigValue.of(() -> creationMode, v -> this.creationMode = v);
    }

    @Override
    public IConfigValue<Boolean> doConvertEmptyTag() {
        return IConfigValue.of(() -> convertEmptyTag, v -> this.convertEmptyTag = v);
    }
}
