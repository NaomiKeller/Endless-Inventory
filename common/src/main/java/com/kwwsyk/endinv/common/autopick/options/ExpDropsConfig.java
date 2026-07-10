package com.kwwsyk.endinv.common.autopick.options;


import com.kwwsyk.endinv.common.options.config.ComplexConfigEntryImpl;
import com.kwwsyk.endinv.common.options.config.ConfigEntryImpl;

/**Configuration on behaviors of experience orbs.<p>
 * {@link #PROTECT_DROPS} give invulnerability for 5 min for every orb.<br>
 * The 3 remain configurations can effect individually and some overrides other logically.<br>
 * <l>
 *     {@link #DIRECTED_DISTRIBUTE} gives orbs a velocity towards player.<br>
 *     {@link #TOUCH_DIRECTLY} let orbs be touched directly by player. And then the orb shall discard, prevent {@code distribute}<br>.
 *     {@link #GIVE_DIRECTLY} let player directly increase exp on killing mobs or mining, jumping the summoning of orbs.
 *          And all living orbs from other sources discard with increasing player's exp value. This entry can cause conflictions show as bugs
 *          with some mods whose features rely on exp orbs' creating or {@code playerTouch} process.
 * </l>
 * <l>
 *     For exp from loot:<br>
 *      {@code give} > {@code touch} > {@code distribute}<p>
 *     For living exp:<br>
 *      {@code touch} > {@code give} > {@code distribute}</p>
 * </l>
 *
 * @since 26/3/2 - in dev
 * @version dev
 */
public class ExpDropsConfig extends ComplexConfigEntryImpl<ExpDropsConfig.Param> {

    public ExpDropsConfig(String key) {
        super(key);
    }

    public record Param(
            boolean protectDrops,
            int directedDistribute,
            boolean touchDirectly, boolean giveDirectly
    ){
        public static final Param DEFAULT = new Param(true,-1, false, false);
    }

    public final BooleanEntry PROTECT_DROPS =
            new BooleanEntry("protect_drops",
                    new String[]{"Give orbs invulnerability from being destroyed by fire, explosion..."},
                    true);
    public final IntEntry DIRECTED_DISTRIBUTE =
            new IntEntry("directed_distribute",
                    new String[]{
                            "Let orbs fly towards the NEAREST player's in 16 blocks direction instead of an initial speed with random direction.",
                            "values: = 0: off;",
                            "        values < 0: TP to player directly;",
                            "        values > 1: multiply velocity with 1b/tick * distance, recommend 3."
                    },
                    -1);
    public final BooleanEntry TOUCH_DIRECTLY =
            new BooleanEntry("touch_directly",
                    new String[]{
                            "Let the exp be touched by the NEAREST player in 16 blocks"
                    },
                    false
            );
    public final BooleanEntry GIVE_DIRECTLY =
            new BooleanEntry("give_directly",
                    new String[]{
                            "Directly give the looter the dropped exp.",
                            "This jump out summon and touch of exp orbs, ",
                            "so bugs may arise as confliction with mods relevant to exp motivations, e.g. pufferfish's skills"
                    },
                    false
            );

    @Override
    public ConfigEntryImpl<?>[] fields() {
        return new ConfigEntryImpl[]{
                PROTECT_DROPS, DIRECTED_DISTRIBUTE,TOUCH_DIRECTLY, GIVE_DIRECTLY
        };
    }
}
