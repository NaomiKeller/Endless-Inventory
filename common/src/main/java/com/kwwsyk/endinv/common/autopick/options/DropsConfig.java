package com.kwwsyk.endinv.common.autopick.options;


import com.kwwsyk.endinv.common.options.config.ComplexConfigEntryImpl;
import com.kwwsyk.endinv.common.options.config.ConfigEntryImpl;

public class DropsConfig extends ComplexConfigEntryImpl<DropsConfig.Param> {

    public DropsConfig(String key) {
        super(key);
    }

    public record Param(
            boolean protectDrops,
            int directedDistribute,
            boolean sendToInventory,
            boolean directlySendToEndinv
    ){
        public static final Param DEFAULT = new Param(true,-1,false, false);
    }

    /// Impl
    /// see {@code neoforge.event.LootEvent}
    /// @see com.kwwsyk.endinv.neoforge.events.LootEvent


    //impled by event, for both block and entity drops
    public final BooleanEntry PROTECT_DROPS =
            new BooleanEntry("protect_drops",
                    new String[]{"Give drops invulnerability from being destroyed by fire, explosion..."},
                    true);
    public final IntEntry DIRECTED_DISTRIBUTE =
            new IntEntry("directed_distribute",
                    new String[]{
                            "Let drops fly towards breaker's direction instead of random direction initial speed.",
                            "values: = 0: off;",
                            "        values < 0: TP to player directly;",
                            "        values > 1: multiply velocity with 1b/tick * distance, recommend 3."
                    },
                    0);
    public final BooleanEntry SEND_TO_INVENTORY =
            new BooleanEntry("send_to_inventory",
                    new String[]{
                            "Directly simulate player touch to send drops to inventory.",
                            "This will not block drops direction or protection if inventory is full."
                    },
                    false
            );
    public final BooleanEntry DIRECTLY_SEND_TO_ENDINV =
            new BooleanEntry(
                    "directly_send_to_endinv",
                    new String[]{
                            "Directly send items to endinv,",
                            "It is the legacy auto pick option"
                    },
                    false
            );
    public final BooleanEntry ENDINV_AFTER_INVENTORY = new BooleanEntry(
            "send_to_endinv_after_touch",
            new String[]{
                    "Send items to endinv after sending to inventory,",
                    "it works in player vanilla picking item session.",
                    "Also need to enable 'send_to_inventory'.",
                    "This is to avoid possible mod conflictions"
            },
            true
    );
    public final BooleanEntry PICK_TO_ENDINV = new BooleanEntry(
            "pick_to_endinv",
            new String[]{
                    "Let all item in vanilla picking item session to endinv.",
                    "This equals to 'send_to_endinv_after_touch' with send picked on ground items to endinv."
            },
            false
    );

    @Override
    public ConfigEntryImpl<?>[] fields() {
        return new ConfigEntryImpl[]{
                PROTECT_DROPS, DIRECTED_DISTRIBUTE, SEND_TO_INVENTORY, DIRECTLY_SEND_TO_ENDINV, ENDINV_AFTER_INVENTORY, PICK_TO_ENDINV
        };
    }
}
