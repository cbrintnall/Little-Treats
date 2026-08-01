package com.odder.littletreat.mixin.farmersdelight;

import com.odder.littletreat.event.RecipeAssembledEvent;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.crafting.DoughRecipe;

@Mixin(DoughRecipe.class)
public class DoughRecipeMixin {
    @Inject(method = "assemble", at = @At("TAIL"))
    private void littletreat$assemble(CraftingInput container, HolderLookup.Provider registryAccess, CallbackInfoReturnable<ItemStack> cir) {
        new RecipeAssembledEvent(container.items(), cir.getReturnValue(), RecipeType.CRAFTING).post();
    }
}
