package com.odder.littletreat;

import com.odder.littletreat.command.ClientCommands;
import com.odder.littletreat.command.TreatCommands;
import com.odder.littletreat.init.Attachments;
import com.odder.littletreat.init.DataComponents;
import com.odder.littletreat.init.Registries;
import com.odder.littletreat.payload.SyncModificationsPayload;
import com.odder.littletreat.player.HealthRegen;
import com.odder.littletreat.processing.FoodDispatcher;
import com.odder.littletreat.processing.RecipeProcessor;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;

@Mod(LittleTreat.MODID)
public class LittleTreat {
    public static final String MODID = "littletreat";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final HealthRegen healthRegen = new HealthRegen();

    public static boolean FARMERS_DELIGHT_PRESENT = false;

    public LittleTreat(IEventBus modEventBus, ModContainer modContainer) {
        FARMERS_DELIGHT_PRESENT = ModList.get().isLoaded("farmersdelight");

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        NeoForge.EVENT_BUS.register(FoodDispatcher.INSTANCE);

        Attachments.ATTACHMENTS.register(modEventBus);
        DataComponents.COMPONENTS.register(modEventBus);

        modEventBus.addListener(Registries::registerDatapackRegistries);
        modEventBus.addListener(LittleTreat::registerPayloads);

        NeoForge.EVENT_BUS.register(ClientCommands.class);
        NeoForge.EVENT_BUS.addListener(RecipeProcessor.INSTANCE::onRecipeAssembledEvent);
        NeoForge.EVENT_BUS.addListener(TreatCommands::onRegisterCommands);
        NeoForge.EVENT_BUS.register(healthRegen);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToClient(
                        SyncModificationsPayload.TYPE,
                        SyncModificationsPayload.STREAM_CODEC,
                        SyncModificationsPayload::handle
                );
    }
}
