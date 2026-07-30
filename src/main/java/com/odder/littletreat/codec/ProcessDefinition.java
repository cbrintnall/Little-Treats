package com.odder.littletreat.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;

public record ProcessDefinition(
    RecipeType<?> type,
    List<QuantityModificationDefinition> modifications
) {
    public static final Codec<ProcessDefinition> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            BuiltInRegistries.RECIPE_TYPE.byNameCodec().fieldOf("type").forGetter(ProcessDefinition::type),
            QuantityModificationDefinition.CODEC.listOf().fieldOf("modifications").forGetter(ProcessDefinition::modifications)
    ).apply(inst, ProcessDefinition::new));
}
