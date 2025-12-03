package com.kwwsyk.endinv.forge;

import com.kwwsyk.endinv.common.options.ServerConfigs;
import com.kwwsyk.endinv.common.options.config.ConfigEntryImpl;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ServerConfig {

    public static final ServerConfig CONFIG;
    public static final ForgeConfigSpec CONFIG_SPEC;

    private ServerConfig(ForgeConfigSpec.Builder builder){
        ServerConfigs.getConfigs().forEach(cfg->{
            cfg.setSaver(()->CONFIG_SPEC.save());
            recursiveBuild(builder, cfg);
        }
        );
    }

    @SuppressWarnings("unchecked")
    private static <T, E extends Enum<E>> void recursiveBuild(ForgeConfigSpec.Builder builder, ConfigEntryImpl<T> cfg){
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
                ForgeConfigSpec.ConfigValue<T> cv = (ForgeConfigSpec.ConfigValue<T>) builder.comment(enumEntry.comments()).defineEnum(enumEntry.key(),(E)enumEntry.defaultValue());
                cfg.initialize(cv, cv::set);
            }
            case com.kwwsyk.endinv.common.options.config.ConfigEntryImpl.ListEntry<?> listEntry -> {
                ForgeConfigSpec.ConfigValue cv = builder.comment(listEntry.comments()).defineListAllowEmpty(listEntry.key(),listEntry.defaultValue(),listEntry.getNewValPredicate());
                listEntry.initialize(cv, cv::set);
            }
            case com.kwwsyk.endinv.common.options.config.ConfigEntryImpl.FloatEntry floatEntry -> {
                var cv = builder.comment(floatEntry.comments()).defineInRange(floatEntry.key(), floatEntry.defaultValue(), floatEntry.getMin(), floatEntry.getMax());
                floatEntry.initialize(cv, cv::set);
            }
            case com.kwwsyk.endinv.common.options.config.ConfigEntryImpl.LongEntry longEntry -> {
                var cv = builder.comment(longEntry.comments()).defineInRange(longEntry.key(), longEntry.defaultValue(), longEntry.getMin(), longEntry.getMax());
                longEntry.initialize(cv,cv::set);
            }
            case com.kwwsyk.endinv.common.options.config.ComplexConfigEntryImpl<?> complexEntry -> {
                builder.push(complexEntry.key());
                builder.comment(complexEntry.comments());
                builder.comment("");
                for(ConfigEntryImpl<?> field : complexEntry.fields()) recursiveBuild(builder,field);
                complexEntry.setInitialized();
                builder.pop();
            }
            default -> {
                ForgeConfigSpec.ConfigValue<T> cv = builder.comment(cfg.comments()).define(cfg.key(), cfg.defaultValue());//good luck to you
                cfg.initialize(cv, cv::set);
            }
        }
    }

    static {
        Pair<ServerConfig, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }
}
