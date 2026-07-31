package com.odder.littletreat.player;

import com.odder.littletreat.Config;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;

/**
 * Replaces the FoodData class on the player so we can swap in our functionality
 * while being minimally disruptive.
 */
public class LittleTreatFoodData extends FoodData {
    private static final int DEFAULT_FOOD_LEVEL = 20;
    private static final float DEFAULT_SATURATION_LEVEL = 5.0f;
    private static final float DEFAULT_EXHAUSTION_LEVEL = 0.0f;

    @Override
    public void tick(Player player) {
        if (!Config.DISABLE_VANILLA_HUNGER.get()) {
            super.tick(player);
        }
    }

    @Override
    public boolean needsFood() {
        // false since this behavior is delegated to the player class now, which calls this
        if (Config.DISABLE_VANILLA_HUNGER.get()) {
            return true;
        } else {
            return super.needsFood();
        }
    }

    @Override
    public float getExhaustionLevel() {
        if (Config.DISABLE_VANILLA_HUNGER.get()) {
            return DEFAULT_EXHAUSTION_LEVEL;
        } else {
            return super.getExhaustionLevel();
        }
    }

    @Override
    public void eat(int foodLevelModifier, float saturationLevelModifier) {
        if (!Config.DISABLE_VANILLA_HUNGER.get()) {
            super.eat(foodLevelModifier, saturationLevelModifier);
        }
    }

    @Override
    public void eat(FoodProperties foodProperties) {
        if (!Config.DISABLE_VANILLA_HUNGER.get()) {
            super.eat(foodProperties);
        }
    }

    @Override
    public void addExhaustion(float exhaustion) {
        if (!Config.DISABLE_VANILLA_HUNGER.get()) {
            super.addExhaustion(exhaustion);
        }
    }

    @Override
    public float getSaturationLevel() {
        if (Config.DISABLE_VANILLA_HUNGER.get()) {
            return DEFAULT_SATURATION_LEVEL;
        }  else {
            return super.getSaturationLevel();
        }
    }

    @Override
    public int getFoodLevel() {
        if  (Config.DISABLE_VANILLA_HUNGER.get()) {
            return DEFAULT_FOOD_LEVEL;
        } else {
            return super.getFoodLevel();
        }
    }
}
