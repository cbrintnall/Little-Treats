package com.odder.littletreat.processing;

import com.odder.littletreat.LittleTreat;
import com.odder.littletreat.codec.AttributeModificationDefinition;
import com.odder.littletreat.codec.FoodDefinition;
import com.odder.littletreat.init.DataComponents;
import com.odder.littletreat.init.Registries;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class RecipeProcessor {
    public static RecipeProcessor INSTANCE = new RecipeProcessor();

    private HashMap<Item, List<AttributeModificationDefinition>> attrCache = new HashMap<>();

    public void process(Collection<ItemStack> inputs, ItemStack output, RecipeType<?> type) {
        var modifiers = inputs
                .stream()
                .map(stack -> getModificationsFor(stack.getItemHolder()))
                .flatMap(Collection::stream)
                .toList();

        modifiers = getFlattenModifiers(modifiers);

        output.set(DataComponents.INHERITED_MODIFICATIONS, modifiers);

        LittleTreat.LOGGER.debug(
                "Created item {} from {} with modifications: {}",
                output.getDisplayName().getString(),
                type,
                modifiers.stream().map(mod -> mod.attribute().getKey().location().toLanguageKey()).collect(Collectors.joining())
        );
    }

    private List<AttributeModificationDefinition> getFlattenModifiers(Collection<AttributeModificationDefinition> mods) {
        HashMap<Holder<Attribute>, List<AttributeModificationDefinition>> similar = new HashMap<>();
        List<AttributeModificationDefinition> flattened = new ArrayList<>();

        for(AttributeModificationDefinition mod : mods) {
            if (!similar.containsKey(mod.attribute())) {
                similar.put(mod.attribute(), new ArrayList<>());
            }

            similar.get(mod.attribute()).add(mod);
        }

        for (Holder<Attribute> attr : similar.keySet()) {
            HashMap<AttributeModifier.Operation, List<AttributeModificationDefinition>> similarOperations = new HashMap<>();
            for (AttributeModificationDefinition mod : similar.get(attr)) {
                if (!similarOperations.containsKey(mod.op())) {
                    similarOperations.put(mod.op(), new ArrayList<>());
                }

                similarOperations.get(mod.op()).add(mod);
            }

            for (AttributeModifier.Operation op :  similarOperations.keySet()) {
                var res = similarOperations.get(op).stream().reduce(
                        (a, b) -> new AttributeModificationDefinition(
                                attr,
                                a.amount()+b.amount(),
                                op,
                                a.duration()+b.duration()
                        )
                );

                res.ifPresent(flattened::add);
            }
        }

        return flattened;
    }

    private List<AttributeModificationDefinition> getModificationsFor(Holder<Item> item) {
        List<AttributeModificationDefinition> modifications = new ArrayList<>();

        var server = ServerLifecycleHooks.getCurrentServer();

        if (server == null) {
            LittleTreat.LOGGER.error("Tried to collect modifications, but theres no server?");
            return modifications;
        }

        var registry = server.registryAccess().registry(Registries.FOOD_DEFINITIONS);

        registry.ifPresent(registryEntry -> {
            registryEntry.asHolderIdMap().forEach(con -> {
                FoodDefinition definition = con.value();
                HolderSet<Item> items = definition.items();

                if (items.contains(item)) {
                    modifications.addAll(definition.modifications());
                }
            });
        });

        return modifications;
    }
}
