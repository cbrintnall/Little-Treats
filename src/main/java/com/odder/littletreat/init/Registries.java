package com.odder.littletreat.init;

import com.mojang.serialization.MapCodec;
import com.odder.littletreat.LittleTreat;
import com.odder.littletreat.codec.FoodDefinition;
import com.odder.littletreat.processing.ComposedFoodLootModifier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class Registries {
    public static final ResourceKey<Registry<FoodDefinition>> FOOD_DEFINITIONS =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(LittleTreat.MODID, "food_definitions"));

    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, LittleTreat.MODID);

    public static final Supplier<MapCodec<ComposedFoodLootModifier>> COMPOSED_FOOD_LOOT_MODIFIER =
            LOOT_MODIFIER_SERIALIZERS.register("composed_food_loot_modifier", () -> ComposedFoodLootModifier.CODEC);

    public static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(FOOD_DEFINITIONS, FoodDefinition.CODEC, FoodDefinition.CODEC);
    }
}
