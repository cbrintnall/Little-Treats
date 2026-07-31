package com.odder.littletreat.mixin;

import com.mojang.authlib.GameProfile;
import com.odder.littletreat.player.LittleTreatFoodData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerMixin {
    @Shadow protected FoodData foodData;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void littletreat$replaceFoodData(Level level, BlockPos pos, float yRot, GameProfile gameProfile, CallbackInfo ci) {
        this.foodData = new LittleTreatFoodData();
    }
}
