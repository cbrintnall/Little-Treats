package com.odder.littletreat.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.odder.littletreat.QuantityModification;
import net.minecraft.util.StringRepresentable;

public record QuantityModificationDefinition(
        double amount,
        QuantityModification modification
) {
    public static final Codec<QuantityModificationDefinition> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.DOUBLE.fieldOf("amount").forGetter(QuantityModificationDefinition::amount),
            StringRepresentable.fromEnum(QuantityModification::values).fieldOf("modification").forGetter(QuantityModificationDefinition::modification)
    ).apply(inst, QuantityModificationDefinition::new));
}
