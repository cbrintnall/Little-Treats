package com.odder.littletreat.mixin;

import com.odder.littletreat.event.RecipeAssembledEvent;
import com.odder.littletreat.processing.RecipeProcessor;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShapelessRecipe.class)
public abstract class ShapelessRecipeMixin {
    @Inject(method = "assemble", at = @At("RETURN"), cancellable = true)
    private void littletreat$assemble(CraftingInput input, HolderLookup.Provider registries, CallbackInfoReturnable<ItemStack> ci) {
        RecipeAssembledEvent.fromIngredients(input, ci.getReturnValue(), RecipeType.CRAFTING).post();
    }
}
