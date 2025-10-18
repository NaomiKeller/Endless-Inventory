package com.kwwsyk.endinv.neoforge.options;

import com.kwwsyk.endinv.common.options.ContentTransferMode;
import com.kwwsyk.endinv.common.options.IConfigValue;
import com.kwwsyk.endinv.common.options.IServerConfig;
import com.kwwsyk.endinv.common.options.MissingEndInvPolicy;
import com.kwwsyk.endinv.common.util.Accessibility;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ServerConfig {

    public static final ServerConfig CONFIG;
    public static final ModConfigSpec CONFIG_SPEC;
    public final ModConfigSpec.IntValue MAX_STACK_SIZE;
    public final ModConfigSpec.BooleanValue ENABLE_INFINITE;
    public final ModConfigSpec.BooleanValue ENABLE_AUTO_PICK;
    public final ModConfigSpec.EnumValue<ContentTransferMode> TRANSFER_MODE;
    public final ModConfigSpec.EnumValue<Accessibility> DEFAULT_ACCESSIBILITY;
    public final ModConfigSpec.EnumValue<MissingEndInvPolicy> CREATION_MODE;
    public final ModConfigSpec.BooleanValue CONVERT_EMPTY_TAG;

    private ServerConfig(ModConfigSpec.Builder builder){
        MAX_STACK_SIZE = builder
                .translation("config.endinv.comment.max_stack_size")
                .defineInRange("ItemCapacity.maxStackSize",Integer.MAX_VALUE,0,Integer.MAX_VALUE);
        ENABLE_INFINITE = builder
                .translation("config.endinv.comment.enable_infinite1")
                .define("ItemCapacity.enableInfinite",false);
        ENABLE_AUTO_PICK = builder
                .comment("Will enable player to auto pick item and exp")
                .define("autoPickUtility",false);
        TRANSFER_MODE = builder
                .defineEnum("TransferMode",ContentTransferMode.ALL);
        DEFAULT_ACCESSIBILITY = builder
                .defineEnum("defaultAccessibility",Accessibility.PUBLIC);
        CREATION_MODE = builder
                .defineEnum("creationMode",MissingEndInvPolicy.CREATE_PER_PLAYER);
        CONVERT_EMPTY_TAG = builder
                .comment("Convert itemstack with empty tag {} to null tag, for the bugs that item cannot be taken/stacked.")
                .define("convert_empty_tag",true);
    }

    static {
        Pair<ServerConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(ServerConfig::new);
        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }

    public final IServerConfig INSTANCE = new IServerConfig() {

        private static IConfigValue<Integer> convert(ModConfigSpec.IntValue value){
            return IConfigValue.of(value::getAsInt,value::set);
        }

        private static IConfigValue<Boolean> convert(ModConfigSpec.BooleanValue value){
            return IConfigValue.of(value::getAsBoolean,value::set);
        }

        @Override
        public IConfigValue<Integer> getMaxAllowedStackSize() {
            return convert(MAX_STACK_SIZE);
        }

        @Override
        public IConfigValue<Boolean> allowInfinityMode() {
            return convert(ENABLE_INFINITE);
        }

        @Override
        public IConfigValue<Boolean> enableAutoPick() {
            return convert(ENABLE_AUTO_PICK);
        }

        @Override
        public void onAttachingOrAutopickConfigChanged() {
            CONFIG_SPEC.save();
        }

        /**
         * Notify all players that the server's specified menu attachability has changed.
         * Broadcasts an effective attachability snapshot for client-side checks.
         */
        @Override
        public void onSpecifiedMenuAttachabilityChanged() {
            CONFIG_SPEC.save();
        }

        @Override
        public IConfigValue<Boolean> enableAttaching() {
            // NeoForge config: default to true; can be expanded later
            return IConfigValue.of(() -> true, v -> {});
        }

        @Override
        public IConfigValue<ContentTransferMode> transferMode() {
            return IConfigValue.of(TRANSFER_MODE,TRANSFER_MODE::set);
        }

        @Override
        public IConfigValue<Accessibility> defaultAccessibility() {
            return IConfigValue.of(DEFAULT_ACCESSIBILITY,DEFAULT_ACCESSIBILITY::set);
        }

        @Override
        public IConfigValue<MissingEndInvPolicy> policyHandlingMissing() {
            return IConfigValue.of(CREATION_MODE,CREATION_MODE::set);
        }

        @Override
        public IConfigValue<Boolean> doConvertEmptyTag() {
            return convert(CONFIG.CONVERT_EMPTY_TAG);
        }

        @Override
        public IConfigValue<com.kwwsyk.endinv.common.options.SpecifiedMenuAttachingConfig> specifiedMenuAttachability() {
            // NeoForge: default empty config
            return IConfigValue.of(() -> com.kwwsyk.endinv.common.options.SpecifiedMenuAttachingConfig.DEFAULT, v -> {});
        }
    };
}
