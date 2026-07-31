package com.odder.littletreat;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue EFFECT_MARGIN = BUILDER.defineInRange("margin", 4, 0, 50);
    public static final ModConfigSpec.BooleanValue DISABLE_VANILLA_HUNGER = BUILDER.define("disableVanillaHunger", true);
    public static final ModConfigSpec.IntValue ALLOWED_EFFECT_COUNT = BUILDER.defineInRange("allowedEffectCount", 3, 0, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue TICKS_TO_REGEN = BUILDER.defineInRange("ticksToRegen", 30 * 20, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue TICKS_PER_REGEN = BUILDER.defineInRange("ticksPerRegen", 80, 1, Integer.MAX_VALUE);

    static final ModConfigSpec SPEC = BUILDER.build();
}
