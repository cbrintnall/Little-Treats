package com.odder.littletreat.processing;

import com.odder.littletreat.Config;
import com.odder.littletreat.LittleTreat;
import com.odder.littletreat.client.ClientState;
import com.odder.littletreat.codec.ActiveModificationDefinition;
import com.odder.littletreat.codec.AttributeModificationDefinition;
import com.odder.littletreat.codec.FoodDefinition;
import com.odder.littletreat.init.Attachments;
import com.odder.littletreat.init.DataComponents;
import com.odder.littletreat.init.Registries;
import com.odder.littletreat.payload.SyncModificationsPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;

public class FoodDispatcher {
    public static final FoodDispatcher INSTANCE =  new FoodDispatcher();

    private Queue<Tuple<ActiveModificationDefinition, ServerPlayer>> removals = new ArrayDeque<>();
    private HashMap<ResourceLocation, List<FoodDefinition>> cachedDefinitions = new HashMap<>();

    public FoodDispatcher() {}

    public static List<AttributeModificationDefinition> collectModifications(ItemStack eaten) {
        List<AttributeModificationDefinition> modifications = new ArrayList<>();

        ResourceLocation loc = eaten.getItemHolder().getKey().location();
        if (INSTANCE.cachedDefinitions.containsKey(loc)) {
            var definitions = INSTANCE.cachedDefinitions.get(loc);
            modifications.addAll(definitions.stream().map(FoodDefinition::modifications).flatMap(Collection::stream).toList());
        }

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
    private void onDatapackSync(OnDatapackSyncEvent event) {
        RegistryAccess access = event.getPlayerList().getServer().registryAccess();
        Registry<FoodDefinition> registry = access.registryOrThrow(Registries.FOOD_DEFINITIONS);

        cachedDefinitions.clear();

        for (var entry : registry.entrySet()) {
            entry.getValue().items().forEach(item -> {
                ResourceLocation loc = item.getKey().location();

                if (!cachedDefinitions.containsKey(loc)) {
                    cachedDefinitions.put(loc, new ArrayList<>());
                }

                cachedDefinitions.get(loc).add(entry.getValue());
            });
        }

        LittleTreat.LOGGER.debug("Rebuilt Little Treat item cache, {} items counted", cachedDefinitions.keySet().size());
    }

    @SubscribeEvent
    private void onStart(PlayerInteractEvent.RightClickItem event) {
        Collection<ActiveModificationDefinition> mods = EffectiveSide.get().isClient()
                ? ClientState.INSTANCE.getActive()
                : event.getEntity().getData(Attachments.ACTIVE_MODIFICATIONS);

        int count = mods.size();

        // Allow eating if what we're eating matches an attribute source (to refresh duration)
        if (mods.stream().anyMatch(mod -> mod.source.equals(event.getItemStack().getItemHolder()))) {
            return;
        }

        if (count >= Config.ALLOWED_EFFECT_COUNT.get()) {
            event.setCanceled(true);
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
