package com.odder.littletreat;

import com.odder.littletreat.client.ClientState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = LittleTreat.MODID, dist = Dist.CLIENT)
public class LittleTreatClient {
    public LittleTreatClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        NeoForge.EVENT_BUS.register(ClientState.INSTANCE);
    }
}
