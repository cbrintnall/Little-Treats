package com.odder.littletreat.processing;

import com.odder.littletreat.LittleTreat;
import com.odder.littletreat.codec.ActiveModificationDefinition;
import com.odder.littletreat.codec.AttributeModificationDefinition;
import com.odder.littletreat.init.Attachments;
import com.odder.littletreat.init.DataComponents;
import com.odder.littletreat.init.Registries;
import com.odder.littletreat.payload.SyncModificationsPayload;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.*;

public class FoodDispatcher {
    public static final FoodDispatcher INSTANCE =  new FoodDispatcher();

    private Queue<Tuple<ActiveModificationDefinition, ServerPlayer>> removals = new ArrayDeque<>();

    public FoodDispatcher() {}

    public static List<AttributeModificationDefinition> collectModifications(ItemStack eaten) {
        List<AttributeModificationDefinition> modifications = new ArrayList<>();

        var server = ServerLifecycleHooks.getCurrentServer();

        if (server == null) {
            LittleTreat.LOGGER.error("Tried to collect modifications, but theres no server?");
            return modifications;
        }

        var registry = server.registryAccess().registry(Registries.FOOD_DEFINITIONS);

        registry.ifPresent(registryEntry -> {
            registryEntry.asHolderIdMap().forEach(con -> {
                var items = con.value().items();
                if (items.contains(eaten.getItemHolder())) {
                    modifications.addAll(con.value().modifications());
                }
            });
        });

        modifications.addAll(eaten.getOrDefault(DataComponents.INHERITED_MODIFICATIONS, Collections.emptyList()));

        return modifications;
    }

    public void applyModification(Holder<Item> src, AttributeModificationDefinition modification, ServerPlayer player) {
        AttributeInstance attr = player.getAttribute(modification.attribute());

        if (attr == null) return;

        List<ActiveModificationDefinition> activeModifications = player.getData(Attachments.ACTIVE_MODIFICATIONS.get());
        ResourceLocation futureId = ActiveModificationDefinition.createIdFor(src, modification);
        var modifier = attr.getModifier(futureId);

        if (modifier != null) {
            activeModifications
                .stream()
                .filter(mod -> mod.getId().equals(futureId))
                .findFirst()
                .ifPresent(mod -> {
                    mod.resetTicks();
                    player.setData(Attachments.ACTIVE_MODIFICATIONS, activeModifications);
                    SyncModificationsPayload.syncToClient(player);
                    LittleTreat.LOGGER.debug("Reset duration of {}", futureId);
                });

            return;
        }

        ActiveModificationDefinition activeModification = new ActiveModificationDefinition(src, modification);

        attr.addOrUpdateTransientModifier(activeModification.toModifier());

        List<ActiveModificationDefinition> newSet = new ArrayList<>(activeModifications);
        newSet.add(activeModification);
        player.setData(Attachments.ACTIVE_MODIFICATIONS.get(), newSet);
    }

    public void clearAllBuffs(ServerPlayer player) {
        progressPlayerTicks(player, Integer.MAX_VALUE);
    }

    private void progressPlayerTicks(ServerPlayer player, int ticks) {
        List<ActiveModificationDefinition> active = player.getData(Attachments.ACTIVE_MODIFICATIONS.get());

        for(ActiveModificationDefinition modification : active) {
            boolean wasFinished = modification.isFinished();

            modification.progressTicks(ticks);

            if (!wasFinished && modification.isFinished()) {
                removals.add(new Tuple<>(modification, player));
            }
        }
    }

    @SubscribeEvent
    private void onFinish(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;

        List<AttributeModificationDefinition> modifications = collectModifications(event.getItem());

        for(AttributeModificationDefinition modification : modifications) {
            applyModification(event.getItem().getItemHolder(), modification, serverPlayer);
        }

        SyncModificationsPayload.syncToClient(serverPlayer);
    }

    @SubscribeEvent
    private void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        progressPlayerTicks(serverPlayer, 1);
    }

    @SubscribeEvent
    private void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;

        for (ActiveModificationDefinition mod : serverPlayer.getData(Attachments.ACTIVE_MODIFICATIONS.get())) {
            AttributeInstance attr = serverPlayer.getAttribute(mod.def().attribute());
            if (attr != null) attr.addOrUpdateTransientModifier(mod.toModifier());
        }

        SyncModificationsPayload.syncToClient(serverPlayer);
    }

    @SubscribeEvent
    private void onServerTick(ServerTickEvent.Post event) {
        var next = removals.poll();

        if (next != null){
            ServerPlayer player = next.getB();
            ActiveModificationDefinition modification = next.getA();
            var newSet = new ArrayList<>(player.getData(Attachments.ACTIVE_MODIFICATIONS.get()));
            newSet.remove(modification);
            player.setData(Attachments.ACTIVE_MODIFICATIONS.get(), newSet);
            AttributeInstance attr = player.getAttribute(modification.def.attribute());
            attr.removeModifier(modification.getId());
            SyncModificationsPayload.syncToClient(player);
        }
    }
}
