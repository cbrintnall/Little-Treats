package com.odder.littletreat.codec;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.odder.littletreat.Utilities;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

public record FoodDefinition(
        List<String> itemPaths,
        List<AttributeModificationDefinition> modifications
) {
    public static final Codec<FoodDefinition> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.either(
                    Codec.STRING.listOf(),
                    Codec.STRING
            ).xmap(
                    either -> either.map(
                            multiple -> multiple,
                            one -> List.of(one)
                    ),
                    list -> list.size() == 1
                            ? Either.right(list.get(0))
                            : Either.left(list)).fieldOf("items").forGetter(FoodDefinition::itemPaths),
            AttributeModificationDefinition.CODEC.listOf().fieldOf("modifications").forGetter(FoodDefinition::modifications)
    ).apply(inst, FoodDefinition::new));

    public HolderSet<Item> getItems(RegistryAccess access) {
        Registry<Item> itemRegistry = access.registryOrThrow(Registries.ITEM);
        List<Holder<Item>> itemHolders = new ArrayList<>();

        for (String itemPath : itemPaths) {
            if (itemPath.startsWith("#")) {
                itemRegistry.getTag(Utilities.parseTag(itemPath, Registries.ITEM))
                        .ifPresent(tagged -> tagged.stream().filter(Holder::isBound).forEach(itemHolders::add));
            } else {
                ResourceLocation loc = ResourceLocation.parse(itemPath);
                itemRegistry.getHolder(ResourceKey.create(Registries.ITEM, loc))
                        .ifPresent(itemHolders::add);
            }
        }

        return HolderSet.direct(itemHolders);
    }
}
