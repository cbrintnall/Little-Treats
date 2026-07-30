package com.odder.littletreat.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

import java.util.List;

public record FoodDefinition(
        HolderSet<Item> items,
        List<AttributeModificationDefinition> modifications
) {
    public static final Codec<FoodDefinition> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("items").forGetter(FoodDefinition::items),
            AttributeModificationDefinition.CODEC.listOf().fieldOf("modifications").forGetter(FoodDefinition::modifications)
    ).apply(inst, FoodDefinition::new));
}
