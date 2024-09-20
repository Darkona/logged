package com.darkona.toolset.logging.colors;

import com.darkona.toolset.logging.ColorEnum;

public enum Brown implements ColorEnum {

    CORNSILK((short) 255, (short) 248, (short) 220),
    BLANCHED_ALMOND((short) 255, (short) 235, (short) 205),
    BISQUE((short) 255, (short) 228, (short) 196),
    NAVAJO_WHITE((short) 255, (short) 222, (short) 173),
    WHEAT((short) 245, (short) 222, (short) 179),
    BURLY_WOOD((short) 222, (short) 184, (short) 135),
    TAN((short) 210, (short) 180, (short) 140),
    ROSY_BROWN((short) 188, (short) 143, (short) 143),
    SANDY_BROWN((short) 244, (short) 164, (short) 96),
    GOLDENROD((short) 218, (short) 165, (short) 32),
    DARK_GOLDENROD((short) 184, (short) 134, (short) 11),
    PERU((short) 205, (short) 133, (short) 63),
    CHOCOLATE((short) 210, (short) 105, (short) 30),
    SADDLE_BROWN((short) 139, (short) 69, (short) 19),
    SIENNA((short) 160, (short) 82, (short) 45),
    BROWN((short) 165, (short) 42, (short) 42),
    MAROON((short) 128, (short) 0, (short) 0);




    private final short r;
    private final short g;
    private final short b;


    Brown(short r, short g, short b) {

        this.r = r;
        this.g = g;
        this.b = b;
    }

    @Override
    public String toString() {
        return assemble(r, g, b);
    }
}
