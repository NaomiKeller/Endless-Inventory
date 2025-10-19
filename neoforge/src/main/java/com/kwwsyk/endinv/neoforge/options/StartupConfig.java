package com.kwwsyk.endinv.neoforge.options;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class StartupConfig {

    public static final StartupConfig CONFIG;
    public static final ModConfigSpec CONFIG_SPEC;

    public final ModConfigSpec.BooleanValue ENABLE_VANILLA_RECIPEBOOK_TRANSFER;
    public final ModConfigSpec.BooleanValue ENABLE_JEI_ATTACHED_TRANSFER;

    static {
        Pair<StartupConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(StartupConfig::new);
        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }

    private StartupConfig(ModConfigSpec.Builder builder) {
        builder.push("mixin");
        ENABLE_VANILLA_RECIPEBOOK_TRANSFER = builder
                .comment("Enable mixins that integrate vanilla Recipe Book transfer with Endless Inventory.")
                .define("vanillaRecipeBookTransfer", true);
        builder.pop();

        builder.push("experimental");
        ENABLE_JEI_ATTACHED_TRANSFER = builder
                .comment("Enable experimental JEI recipe transfer integration with attached Endless Inventory.")
                .define("jeiAttachedRecipeTransfer", false);
        builder.pop();
    }

    public static boolean enableVanillaRecipeBookTransfer() {
        return CONFIG.ENABLE_VANILLA_RECIPEBOOK_TRANSFER.get();
    }

    public static boolean enableJeiAttachedTransfer() {
        return CONFIG.ENABLE_JEI_ATTACHED_TRANSFER.get();
    }
}
