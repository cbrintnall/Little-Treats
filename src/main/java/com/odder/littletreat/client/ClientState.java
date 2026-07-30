package com.odder.littletreat.client;

import com.odder.littletreat.codec.ActiveModificationDefinition;
import com.odder.littletreat.codec.AttributeModificationDefinition;
import com.odder.littletreat.processing.FoodDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ClientState {
    public static final ClientState INSTANCE = new ClientState();

    private List<ActiveModificationDefinition> active = new ArrayList<>();

    public void setActive(List<ActiveModificationDefinition> active) {
        this.active = active;
    }

    public Collection<ActiveModificationDefinition> getActive() {
        return active;
    }

    @SubscribeEvent
    private void onInventoryRender(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof EffectRenderingInventoryScreen<?> inventoryScreen) {
            GuiGraphics gfx = event.getGuiGraphics();
            GuiDraw.drawInventoryActiveModifications(inventoryScreen, getActive(), gfx, event.getMouseX(), event.getMouseY());
        }
    }

    @SubscribeEvent
    private void onClientTick(ClientTickEvent.Post event) {
        for(ActiveModificationDefinition activeModificationDefinition : active) {
            activeModificationDefinition.remainingTicks--;
        }
    }

    @SubscribeEvent
    private void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui) return;

        GuiGraphics gui = event.getGuiGraphics();
        GuiDraw.drawActiveModifications(getActive(), gui, 4, 4);
    }

    @SubscribeEvent
    private void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<AttributeModificationDefinition> mods = FoodDispatcher.collectModifications(stack);

        if (mods.isEmpty()) return;

        for (AttributeModificationDefinition mod : mods) {
            Component value = GuiDraw.formatModifier(mod.amount(), mod.op());

            event.getToolTip().add(
                    Component.translatable(mod.attribute().value().getDescriptionId()).append(": ").append(value));
        }
    }
}
