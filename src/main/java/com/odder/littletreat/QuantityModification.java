package com.odder.littletreat;

import net.minecraft.util.StringRepresentable;

public enum QuantityModification implements StringRepresentable {
    ADD("add"),
    SCALE("scale"),
    SET("set");

    private final String name;

    QuantityModification(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
