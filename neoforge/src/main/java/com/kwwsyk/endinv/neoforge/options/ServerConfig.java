package com.kwwsyk.endinv.neoforge.options;

import com.kwwsyk.endinv.common.options.*;
import com.kwwsyk.endinv.common.options.config.ConfigEntryImpl;
import com.kwwsyk.endinv.common.options.config.IConfigValue;
import com.kwwsyk.endinv.common.util.Accessibility;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Supplier;

import static com.kwwsyk.endinv.common.options.ServerConfigs.*;

public class ServerConfig {

    public static final ServerConfig CONFIG;
    public static final ModConfigSpec CONFIG_SPEC;

    //public final ModConfigSpec.ListValueSpec SMC;

    private ServerConfig(ModConfigSpec.Builder builder){
        ServerConfigs.getConfigs().forEach(cfg->{
            cfg.setSaver(() -> CONFIG_SPEC.save());//don't change it to method reference; else NPE thrown
            recursiveBuild(builder,cfg);
        });
    }

    @SuppressWarnings("unchecked")
    private static <T, E extends Enum<E>> void recursiveBuild(ModConfigSpec.Builder builder, ConfigEntryImpl<T> cfg){
        switch (cfg){
            case com.kwwsyk.endinv.common.options.config.ConfigEntryImpl.BooleanEntry booleanEntry -> {
                var configValue = builder.comment(booleanEntry.comments()).define(booleanEntry.key(), booleanEntry.defaultValue());
                booleanEntry.initialize(configValue,configValue::set);
            }
            case com.kwwsyk.endinv.common.options.config.ConfigEntryImpl.IntEntry intEntry -> {
                var cv = builder.comment(intEntry.comments()).defineInRange(intEntry.key(), intEntry.defaultValue(), intEntry.getMin(), intEntry.getMax());
                intEntry.initialize(cv,cv::set);
            }
            case com.kwwsyk.endinv.common.options.config.ConfigEntryImpl.StringEntry stringEntry -> {
                var cv = builder.comment(stringEntry.comments()).define(stringEntry.key(), stringEntry.defaultValue());
                stringEntry.initialize(cv,cv::set);
            }
            case com.kwwsyk.endinv.common.options.config.ConfigEntryImpl.DoubleEntry doubleEntry -> {
                var cv = builder.comment(doubleEntry.comments()).defineInRange(doubleEntry.key(), doubleEntry.defaultValue(), doubleEntry.getMin(), doubleEntry.getMax());
                doubleEntry.initialize(cv,cv::set);
            }
            case com.kwwsyk.endinv.common.options.config.ConfigEntryImpl.EnumEntry<?> enumEntry -> {//how to deal with the recursive typed param: E extends Enum<E extends Enum<E...>>? /*HERE the (E) transformation is necessary, else compile errors, but IDEA doesn't*/I finally tried wip out Types
                ModConfigSpec.ConfigValue<T> cv = (ModConfigSpec.ConfigValue<T>) builder.comment(enumEntry.comments()).defineEnum(enumEntry.key(),(E)enumEntry.defaultValue());
                cfg.initialize(cv, cv::set);
            }
            case com.kwwsyk.endinv.common.options.config.ConfigEntryImpl.ListEntry<?> listEntry -> {
                var cv = builder.comment(listEntry.comments()).defineListAllowEmpty(listEntry.key(),listEntry.defaultValue(),(Supplier)listEntry.getNewValSupplier(),listEntry.getNewValPredicate());
                listEntry.initialize(cv, cv::set);
            }
            case com.kwwsyk.endinv.common.options.config.ConfigEntryImpl.FloatEntry floatEntry -> {
                var cv = builder.comment(floatEntry.comments()).defineInRange(floatEntry.key(), floatEntry.defaultValue(), floatEntry.getMin(), floatEntry.getMax());
                floatEntry.initialize(()-> cv.get().floatValue(), f -> cv.set((double) f));
            }
            case com.kwwsyk.endinv.common.options.config.ConfigEntryImpl.LongEntry longEntry -> {
                var cv = builder.comment(longEntry.comments()).defineInRange(longEntry.key(), longEntry.defaultValue(), longEntry.getMin(), longEntry.getMax());
                longEntry.initialize(cv,cv::set);
            }
            case com.kwwsyk.endinv.common.options.config.ComplexConfigEntryImpl<?> complexEntry -> {
                builder.push(complexEntry.key());
                builder.comment(complexEntry.comments());
                builder.comment("");
                for(com.kwwsyk.endinv.common.options.config.ConfigEntryImpl<?> field : complexEntry.fields()) recursiveBuild(builder,field);
                builder.pop();
            }
            default -> {
                ModConfigSpec.ConfigValue<T> cv = builder.comment(cfg.comments()).define(cfg.key(), cfg.defaultValue());//good luck to you
                cfg.initialize(cv, cv::set);
            }
        }
    }

    static {
        Pair<ServerConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(ServerConfig::new);
        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }

    public final IServerConfig INSTANCE = new IServerConfig() {

        private static com.kwwsyk.endinv.common.options.config.IConfigValue<Integer> convert(ModConfigSpec.IntValue value){
            return com.kwwsyk.endinv.common.options.config.IConfigValue.of(value::getAsInt,value::set);
        }

        private static com.kwwsyk.endinv.common.options.config.IConfigValue<Boolean> convert(ModConfigSpec.BooleanValue value){
            return com.kwwsyk.endinv.common.options.config.IConfigValue.of(value::getAsBoolean,value::set);
        }

        @Override
        public com.kwwsyk.endinv.common.options.config.IConfigValue<Integer> getMaxAllowedStackSize() {
            return ENDINV_BEHAVIOR.MaxStackSize;
        }

        @Override
        public com.kwwsyk.endinv.common.options.config.IConfigValue<Boolean> allowInfinityMode() {
            return ENDINV_BEHAVIOR.EnableInfinity;
        }

        @Override
        public com.kwwsyk.endinv.common.options.config.IConfigValue<Boolean> enableAutoPick() {
            return ENABLE_AUTOPICK;
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
        public com.kwwsyk.endinv.common.options.config.IConfigValue<Boolean> enableAttaching() {
            // NeoForge config: default to true; can be expanded later
            return DEFAULT_ATTACH;
        }

        @Override
        public com.kwwsyk.endinv.common.options.config.IConfigValue<ContentTransferMode> transferMode() {
            return ENDINV_BEHAVIOR.TransferMode;
        }

        @Override
        public com.kwwsyk.endinv.common.options.config.IConfigValue<Accessibility> defaultAccessibility() {
            return ENDINV_BEHAVIOR.Access;
        }

        @Override
        public com.kwwsyk.endinv.common.options.config.IConfigValue<CreationEndinvStrategy> policyHandlingMissing() {
            return CREATION_MODE;
        }

        @Override@Deprecated
        public com.kwwsyk.endinv.common.options.config.IConfigValue<Boolean> doConvertEmptyTag() {
            return null;
        }

        @Override
        public IConfigValue<SpecifiedMenuAttachingConfig> specifiedMenuAttachability() {
            // NeoForge: default empty config
            return SPECIFIED_ATTACHABILITY;
        }
    };
}
