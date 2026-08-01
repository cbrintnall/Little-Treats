package com.odder.littletreat.mixin.farmersdelight;

import com.odder.littletreat.event.RecipeAssembledEvent;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;

import java.util.List;

@Mixin(CookingPotRecipe.class)
public abstract class CookingPotRecipeMixin {
    @Shadow abstract RecipeType<?> getType();

    @Inject(method = "assemble", at = @At("TAIL"))
    private void littletreat$assemble(RecipeWrapper inv, HolderLookup.Provider provider, CallbackInfoReturnable<ItemStack> cir) {
        RecipeAssembledEvent.fromIngredients(inv, cir.getReturnValue(), getType()).post();
    }
}
