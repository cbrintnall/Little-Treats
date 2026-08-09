package com.odder.littletreat.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;

public class TreatUI {
    public static class Anchors {
        public static int hungerBarLeft()   { return guiWidth() / 2 + 10; }
        public static int hungerBarTop()    { return guiHeight() - 49; }
        public static int hungerBarWidth()  { return 81; }
        public static int hungerBarHeight() { return 10; }

        public static int hotbarLeft()      { return guiWidth() / 2 - 91; }
        public static int hotbarTop()       { return guiHeight() - 22; }
        public static int hotbarWidth()     { return 182; }
        public static int hotbarHeight()    { return 22; }
        public static int hotbarCenterX()   { return guiWidth() / 2; }

        private static int guiWidth()  {
            return Minecraft.getInstance().getWindow().getGuiScaledWidth();
        }

        private static int guiHeight() {
            return Minecraft.getInstance().getWindow().getGuiScaledHeight();
        }
    }

    public static int withFullAlpha(ChatFormatting clr) {
        return 0xFF000000 | clr.getColor();
    }
}
