package com.darkona.toolset.logging.colors;

import com.darkona.toolset.logging.ColorEnum;

public enum Red implements ColorEnum {

    INDIAN_RED((short) 205, (short) 92, (short) 92),
    LIGHT_CORAL((short) 240, (short) 128, (short) 128),
    SALMON((short) 250, (short) 128, (short) 114),
    DARK_SALMON((short) 233, (short) 150, (short) 122),
    LIGHT_SALMON((short) 255, (short) 160, (short) 122),
    CRIMSON((short) 220, (short) 20, (short) 60),
    RED((short) 255, (short) 0, (short) 0),
    FIRE_BRICK((short) 178, (short) 34, (short) 34),
    DARK_RED((short) 139, (short) 0, (short) 0);


    private final short r;
    private final short g;
    private final short b;

    Red(short r, short g, short b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }


    @Override
    public String toString() {
        return assemble(r, g, b);
    }
}
