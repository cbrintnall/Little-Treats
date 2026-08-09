package com.odder.littletreat.processing;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.odder.littletreat.LittleTreat;
import com.odder.littletreat.codec.AttributeModificationDefinition;
import com.odder.littletreat.codec.FoodDefinition;
import com.odder.littletreat.init.DataComponents;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

import java.util.Collection;
import java.util.List;

public class ComposedFoodLootModifier extends LootModifier {
    public static final MapCodec<ComposedFoodLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            LootModifier.codecStart(inst).apply(inst, ComposedFoodLootModifier::new));

    protected ComposedFoodLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> items, LootContext lootContext) {
        for (ItemStack item : items) {
            if (FoodDispatcher.isFood(item)) {
                List<FoodDefinition> appliedDefinitions = LittleTreat.recipeProcessor.recipeMaster.getPotentialFoodComposition(item.getItemHolder());
                List<AttributeModificationDefinition> attrModifications = appliedDefinitions.stream().map(FoodDefinition::modifications).flatMap(Collection::stream).toList();
                attrModifications = LittleTreat.recipeProcessor.getFlattenModifiers(attrModifications);
                item.set(DataComponents.INHERITED_MODIFICATIONS, attrModifications);
                LittleTreat.LOGGER.debug("detected food {}, modifying potential composition", item.getDisplayName().getString());
            }
        }

        return items;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
