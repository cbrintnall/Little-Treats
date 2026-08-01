package com.odder.littletreat.mixin.letsdo;

import com.odder.littletreat.event.RecipeAssembledEvent;
import com.odder.littletreat.processing.RecipeProcessor;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.satisfy.farm_and_charm.core.recipe.MincerRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.List;

@Mixin(MincerRecipe.class)
public abstract class LetsDoMincerMixin {
    @Shadow abstract RecipeType<?> getType();
    @Shadow private Ingredient input;

    @Inject(method = "getResultItem", at = @At("TAIL"))
    private void littletreat$getResultItem(HolderLookup.Provider provider, CallbackInfoReturnable<ItemStack> ci) {
        RecipeAssembledEvent.fromIngredients(List.of(input), ci.getReturnValue(), RecipeType.CRAFTING).post();
    }
}
