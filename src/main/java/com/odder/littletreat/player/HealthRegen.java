package com.odder.littletreat.player;

import com.odder.littletreat.Config;
import com.odder.littletreat.LittleTreat;
import com.odder.littletreat.codec.ActiveModificationDefinition;
import com.odder.littletreat.init.Attachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import vectorwing.farmersdelight.common.registry.ModEffects;

import java.util.Collection;
import java.util.HashMap;

public class HealthRegen {
    private final float naturalRegenAmount = 1.0f;
    private HashMap<ServerPlayer, Integer> ticksSinceDamage = new HashMap<>();

    public float getMaxNaturalRegenForPlayer(ServerPlayer player) {
        Collection<ActiveModificationDefinition> mods = player.getData(Attachments.ACTIVE_MODIFICATIONS);
        float maxHealth = player.getMaxHealth();
        float activeModPercent = (float)mods.size() / Config.ALLOWED_EFFECT_COUNT.get();

        return Mth.floor(maxHealth * activeModPercent);
    }

    @SubscribeEvent
    private void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (ticksSinceDamage.containsKey(event.getEntity()))
            ticksSinceDamage.remove(event.getEntity());
    }

    @SubscribeEvent
    private void onPlayerHurt(LivingDamageEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ticksSinceDamage.put(serverPlayer, 0);
            LittleTreat.LOGGER.debug("Reset player regen timer {}", serverPlayer.getUUID());
        }
    }

    @SubscribeEvent
    private void onServerTick(ServerTickEvent.Post event) {
        for(ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            int currentAmount = ticksSinceDamage.getOrDefault(player, 0);
            int nextAmount = currentAmount + 1;

            ticksSinceDamage.put(player, nextAmount);

            int ticksPerRegen = Config.TICKS_PER_REGEN.get();

            int baseTicksRequiredToRegen = Config.TICKS_TO_REGEN.get();

            if (LittleTreat.FARMERS_DELIGHT_PRESENT) {
                if (player.getEffect(ModEffects.NOURISHMENT) != null) {
                    baseTicksRequiredToRegen = Math.round(ticksPerRegen*0.6f);
                }
            }

            int requiredTicksToStartRegen = (baseTicksRequiredToRegen + ticksPerRegen);

            boolean canDoNaturalRegen =
                    event.getServer().getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION)
                    && currentAmount >= requiredTicksToStartRegen
                    && Config.DISABLE_VANILLA_HUNGER.get();

            if (canDoNaturalRegen) {
                if (currentAmount % ticksPerRegen > nextAmount % ticksPerRegen) {
                    float maxHealableAmount = getMaxNaturalRegenForPlayer(player);

                    if (player.getHealth() < maxHealableAmount) {
                        player.heal(Math.min(naturalRegenAmount, maxHealableAmount-player.getHealth()));
                    }
                }
            }
        }
    }
}
