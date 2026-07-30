package com.odder.littletreat;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue EFFECT_MARGIN = BUILDER.defineInRange("margin", 4, 0, 50);

    static final ModConfigSpec SPEC = BUILDER.build();
}
