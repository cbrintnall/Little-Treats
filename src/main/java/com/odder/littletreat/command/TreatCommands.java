package com.odder.littletreat.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.odder.littletreat.processing.FoodDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class TreatCommands {
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("littletreat")
                        .requires(src -> src.hasPermission(2))
                        .then(getClearCommand())
                        .then(getAvailableAttributesCommand())
        );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> getClearCommand() {
        return Commands.literal("clearmods")
                .executes(ctx -> clearPlayers(List.of(ctx.getSource().getPlayerOrException())))
                .then(Commands.argument("targets", EntityArgument.players())
                        .executes(ctx -> clearPlayers(EntityArgument.getPlayers(ctx, "targets"))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> getAvailableAttributesCommand() {
        return Commands.literal("attributes")
                .then(Commands.argument("targets", EntityArgument.entities())
                        .executes(ctx -> dumpAttributes(ctx.getSource(), EntityArgument.getEntities(ctx, "targets"))));
    }

    private static int dumpAttributes(CommandSourceStack src, Collection<? extends Entity> entities) {
        var registry = BuiltInRegistries.ATTRIBUTE;

        for (var entity : entities) {
            if (entity instanceof LivingEntity livingEntity) {
                src.sendSystemMessage(Component.literal(livingEntity.getDisplayName().getString()));
                var entityAttributes = ((LivingEntity) entity).getAttributes();
                var filteredEntityAttributes = new ArrayList<Holder<Attribute>>();
                for (var set : registry.entrySet()) {
                    registry.getHolder(set.getKey()).ifPresent(holder -> {
                        if (entityAttributes.hasAttribute(holder)) {
                            filteredEntityAttributes.add(holder);
                        }
                    });
                }
                var keys = filteredEntityAttributes.stream().map(attr -> attr.getKey().location().toString()).collect(Collectors.joining(", "));
                src.sendSystemMessage(Component.literal(keys));
            }
        }

        return 0;
    }

    private static int clearPlayers(Collection<ServerPlayer> players) {
        for(ServerPlayer player : players) {
            FoodDispatcher.INSTANCE.clearAllBuffs(player);
        }
        return 0;
    }
}
