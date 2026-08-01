package com.odder.littletreat.mixin.letsdo;

import com.odder.littletreat.LittleTreat;
import com.odder.littletreat.event.RecipeAssembledEvent;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.satisfy.farm_and_charm.core.block.entity.RoasterBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(RoasterBlockEntity.class)
public class LetsDoRoasterBlockEntityMixin {
    @Inject(
            method = "generateOutputItem(Lnet/minecraft/world/item/crafting/Recipe;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void littletreat$generateOutputItem(Recipe<?> recipe, RegistryAccess access, CallbackInfoReturnable<ItemStack> cir) {
        LittleTreat.LOGGER.info("boom roasted ");

        RoasterBlockEntity roasterBlockEntity = (RoasterBlockEntity) (Object) this;
        List<ItemStack> inputs = this.collectInputs(roasterBlockEntity, recipe);

        new RecipeAssembledEvent(inputs, cir.getReturnValue(), recipe.getType()).post();
    }

    private List<ItemStack> collectInputs(RoasterBlockEntity roasterBlockEntity, Recipe<?> recipe) {
        return roasterBlockEntity
                .getItems()
                .stream()
                .filter(item -> recipe.getIngredients().stream().anyMatch(i -> i.test(item)))
                .toList();
    }
}
