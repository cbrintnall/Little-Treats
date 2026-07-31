package com.odder.littletreat.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.odder.littletreat.LittleTreat;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public record AttributeModificationDefinition(
        Holder<Attribute> attribute,
        double amount,
        AttributeModifier.Operation op,
        int duration
) {
    public static final Codec<AttributeModificationDefinition> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("attribute").forGetter(AttributeModificationDefinition::attribute),
            Codec.DOUBLE.fieldOf("amount").forGetter(AttributeModificationDefinition::amount),
            AttributeModifier.Operation.CODEC.fieldOf("op").forGetter(AttributeModificationDefinition::op),
            Codec.INT.fieldOf("duration").forGetter(AttributeModificationDefinition::duration)
    ).apply(inst, AttributeModificationDefinition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AttributeModificationDefinition> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.holderRegistry(Registries.ATTRIBUTE), AttributeModificationDefinition::attribute,
                    ByteBufCodecs.DOUBLE, AttributeModificationDefinition::amount,
                    AttributeModifier.Operation.STREAM_CODEC, AttributeModificationDefinition::op,
                    ByteBufCodecs.VAR_INT, AttributeModificationDefinition::duration,
                    AttributeModificationDefinition::new);

    public static ResourceLocation createIdFor(Holder<Item> src, AttributeModificationDefinition def) {
        var path = String.format(
                "treat/%s/%s",
                def.attribute().getRegisteredName().replace(":", "_"),
                src.getRegisteredName().replace(":", "_")
        );

        return ResourceLocation.fromNamespaceAndPath(LittleTreat.MODID, path);
    }

    public static int getMaxDuration(Collection<AttributeModificationDefinition> defs) {
        return defs.stream().map(def -> def.duration()).max(Integer::compareTo).orElse(0);
    }

    public AttributeModifier toModifier(Holder<Item> srcItem) {
        return new AttributeModifier(createIdFor(srcItem, this), amount, op);
    }
}
