package com.odder.littletreat.mixin;

import com.odder.littletreat.processing.RecipeProcessor;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(AbstractCookingRecipe.class)
public abstract class AbstractCookingRecipeMixin {
    @Shadow abstract RecipeType<?> getType();

    @Inject(method = "assemble", at = @At("RETURN"), cancellable = true)
    private void propagate(SingleRecipeInput input, HolderLookup.Provider registries, CallbackInfoReturnable<ItemStack> ci) {
        RecipeProcessor.INSTANCE.process(List.of(input.item()), ci.getReturnValue(), getType());
    }
}
