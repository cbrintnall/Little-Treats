package com.odder.littletreat.processing;

import com.odder.littletreat.LittleTreat;
import com.odder.littletreat.codec.FoodDefinition;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.Tags;

import java.util.*;

public class RecipeMaster {
    MinecraftServer server;
    Queue<Holder<Item>> remaining = new ArrayDeque<>();
    HashMap<Holder<Item>, Collection<RecipeHolder<?>>> relevantRecipes;

    public RecipeMaster(MinecraftServer server) {
        this.server = server;
    }

    public void invalidateAndBuild(RegistryAccess registryAccess) {
        var itemRegistry = registryAccess.registry(Registries.ITEM).orElseThrow();
        var foodSet = itemRegistry.getTag(Tags.Items.FOODS).orElseThrow();

        remaining = new ArrayDeque<>(foodSet.stream().toList());
        relevantRecipes =  new HashMap<>();

        // collect only recipes whose output is food so we don't have to search EVERY recipe each time
        for (RecipeHolder<?> recipeHolder : server.getRecipeManager().getRecipes()) {
            ItemStack result = recipeHolder.value().getResultItem(registryAccess);
            if (result.is(Tags.Items.FOODS)) {
                relevantRecipes.putIfAbsent(result.getItemHolder(), new ArrayList<>());
                relevantRecipes.get(result.getItemHolder()).add(recipeHolder);
            }
        }

        LittleTreat.LOGGER.debug("busted recipe master cache");
    }

    /**
     * Given a food, returns a potential composition of that food
     * @param item The output food (eg bread would have three wheat as a composition)
     * @return the FoodDefinitions associated with the constructed composition
     */
    public List<FoodDefinition> getPotentialFoodComposition(Holder<Item> item) {
        if (!relevantRecipes.containsKey(item)) return new ArrayList<>();

        var recipes = relevantRecipes.get(item);
        var definitions = new ArrayList<FoodDefinition>();

        for (RecipeHolder<?> recipeHolder : recipes) {
            var ingredients = recipeHolder.value().getIngredients();
            for (Ingredient ingredient : ingredients) {
                for(ItemStack stack : ingredient.getItems()) {
                    var localDefinitions = FoodDispatcher.INSTANCE.getDefinition(stack);
                    if (!localDefinitions.isEmpty()) {
                        definitions.add(localDefinitions.getFirst());
                        break;
                    }
                }
            }

            // if we weren't able to get the proper amount of modifications (one per ingredient) then reset
            if (definitions.size() != ingredients.size()) {
                definitions = new ArrayList<>();
            }
        }

        return definitions;
    }

    public boolean tryProcessNext() {
        if (remaining.isEmpty()) return false;

/*
        Holder<Item> processing =  remaining.poll();

        for (RecipeHolder<?> recipeHolder : relevantRecipes.get(processing)) {
            var ingredients = recipeHolder.value().getIngredients();
            for (var ingredient : ingredients) {
                ingredient.getItems()
            }
        }
*/

        return true;
    }
}
