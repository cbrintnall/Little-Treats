package com.odder.littletreat.init;

import com.odder.littletreat.LittleTreat;
import com.odder.littletreat.codec.FoodDefinition;
import com.odder.littletreat.codec.ProcessDefinition;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

public class Registries {
    public static final ResourceKey<Registry<FoodDefinition>> FOOD_DEFINITIONS =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(LittleTreat.MODID, "food_definitions"));

    public static final ResourceKey<Registry<ProcessDefinition>> PROCESS_DEFINITIONS =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(LittleTreat.MODID, "process"));

    public static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(FOOD_DEFINITIONS, FoodDefinition.CODEC);
        event.dataPackRegistry(PROCESS_DEFINITIONS, ProcessDefinition.CODEC);
    }
}
