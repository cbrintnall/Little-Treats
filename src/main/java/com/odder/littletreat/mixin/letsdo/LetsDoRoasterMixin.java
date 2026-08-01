package com.odder.littletreat.mixin.letsdo;

import com.odder.littletreat.event.RecipeAssembledEvent;
import com.odder.littletreat.processing.RecipeProcessor;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.satisfy.farm_and_charm.core.recipe.RoasterRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

@Mixin(RoasterRecipe.class)
public abstract class LetsDoRoasterMixin {
    @Shadow
    private NonNullList<Ingredient> inputs;

    @Shadow
    abstract RecipeType<?> getType();

    @Inject(method = "getResultItem", at = @At("TAIL"))
    private void littletreat$getResultItem(HolderLookup.Provider provider, CallbackInfoReturnable<ItemStack> ci) {
        RecipeAssembledEvent.fromIngredients(inputs, ci.getReturnValue(), RecipeType.CRAFTING).post();
    }
}
