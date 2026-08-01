package com.odder.littletreat.mixin.farmersdelight;

import com.odder.littletreat.event.RecipeAssembledEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;

import java.util.List;

@Mixin(CuttingBoardRecipe.class)
public abstract class CuttingBoardRecipeMixin {
    @Shadow abstract RecipeType<?> getType();

    @Inject(method = "rollResults", at = @At("TAIL"))
    private void littletreat$assemble(RandomSource random, int fortuneLevel, RecipeWrapper inventory, CallbackInfoReturnable<List<ItemStack>> cir) {
        for (ItemStack itemStack : cir.getReturnValue()) {
            RecipeAssembledEvent.fromIngredients(inventory, itemStack, getType()).post();
        }
    }
}
