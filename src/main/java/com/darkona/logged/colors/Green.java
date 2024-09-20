package com.darkona.toolset.logging.colors;

import com.darkona.toolset.logging.ColorEnum;

public enum Green implements ColorEnum {

    GREEN_YELLOW((short) 173, (short) 255, (short) 47),
    CHARTREUSE((short) 127, (short) 255, (short) 0),
    LAWN_GREEN((short) 124, (short) 252, (short) 0),
    LIME((short) 0, (short) 255, (short) 0),
    LIME_GREEN((short) 50, (short) 205, (short) 50),
    PALE_GREEN((short) 152, (short) 251, (short) 152),
    LIGHT_GREEN((short) 144, (short) 238, (short) 144),
    MEDIUM_SPRING_GREEN((short) 0, (short) 250, (short) 154),
    SPRING_GREEN((short) 0, (short) 255, (short) 127),
    MEDIUM_SEA_GREEN((short) 60, (short) 179, (short) 113),
    SEA_GREEN((short) 46, (short) 139, (short) 87),
    FOREST_GREEN((short) 34, (short) 139, (short) 34),
    GREEN((short) 0, (short) 128, (short) 0),
    DARK_GREEN((short) 0, (short) 100, (short) 0),
    YELLOW_GREEN((short) 154, (short) 205, (short) 50),
    OLIVE_DRAB((short) 107, (short) 142, (short) 35),
    OLIVE((short) 128, (short) 128, (short) 0),
    DARK_OLIVE_GREEN((short) 85, (short) 107, (short) 47),
    MEDIUM_AQUAMARINE((short) 102, (short) 205, (short) 170),
    DARK_SEA_GREEN((short) 143, (short) 188, (short) 143),
    LIGHT_SEA_GREEN((short) 32, (short) 178, (short) 170),
    DARK_CYAN((short) 0, (short) 139, (short) 139),
    TEAL((short) 0, (short) 128, (short) 128);

    private final short r;
    private final short g;
    private final short b;

    Green(short r, short g, short b) {

        this.r = r;
        this.g = g;
        this.b = b;
    }

    @Override
    public String toString() {
        return assemble(r, g, b);
    }

}
