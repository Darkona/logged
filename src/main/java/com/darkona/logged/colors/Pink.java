package com.darkona.toolset.logging.colors;

import com.darkona.toolset.logging.ColorEnum;

public enum Pink implements ColorEnum {

    PINK((short) 242, (short) 116, (short) 233),
    LIGHT_PINK((short) 255, (short) 182, (short) 193),
    HOT_PINK((short) 255, (short) 105, (short) 180),
    DEEP_PINK((short) 255, (short) 20, (short) 147),
    PALE_VIOLET_RED((short) 219, (short) 112, (short) 147),
    MEDIUM_VIOLET_RED((short) 199, (short) 21, (short) 133);


    private final short r;
    private final short g;
    private final short b;


    Pink(short r, short g, short b) {

        this.r = r;
        this.g = g;
        this.b = b;
    }

    @Override
    public String toString() {
        return assemble(r, g, b);
    }


}
