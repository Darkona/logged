package com.darkona.logged.colors;


public enum Blue implements ColorEnum {

    AQUA((short) 0, (short) 255, (short) 255),
    CYAN((short) 0, (short) 255, (short) 255),
    LIGHT_CYAN((short) 224, (short) 255, (short) 255),
    PALE_TURQUOISE((short) 175, (short) 238, (short) 238),
    AQUAMARINE((short) 127, (short) 255, (short) 212),
    TURQUOISE((short) 64, (short) 224, (short) 208),
    MEDIUM_TURQUOISE((short) 72, (short) 209, (short) 204),
    DARK_TURQUOISE((short) 0, (short) 206, (short) 209),
    CADET_BLUE((short) 95, (short) 158, (short) 160),
    STEEL_BLUE((short) 70, (short) 130, (short) 180),
    LIGHT_STEEL_BLUE((short) 176, (short) 196, (short) 222),
    POWDER_BLUE((short) 176, (short) 224, (short) 230),
    LIGHT_BLUE((short) 173, (short) 216, (short) 230),
    SKY_BLUE((short) 135, (short) 206, (short) 235),
    LIGHT_SKY_BLUE((short) 135, (short) 206, (short) 250),
    DEEP_SKY_BLUE((short) 0, (short) 191, (short) 255),
    DODGER_BLUE((short) 30, (short) 144, (short) 255),
    CORNFLOWER_BLUE((short) 100, (short) 149, (short) 237),
    MEDIUM_SLATE_BLUE((short) 123, (short) 104, (short) 238),
    ROYAL_BLUE((short) 65, (short) 105, (short) 225),
    BLUE((short) 0, (short) 0, (short) 255),
    MEDIUM_BLUE((short) 0, (short) 0, (short) 205),
    DARK_BLUE((short) 0, (short) 0, (short) 139),
    NAVY((short) 0, (short) 0, (short) 128),
    MIDNIGHT_BLUE((short) 25, (short) 25, (short) 112);

    private final short r;
    private final short g;
    private final short b;


    Blue(short r, short g, short b) {

        this.r = r;
        this.g = g;
        this.b = b;
    }

    @Override
    public String toString() {
        return assemble(r, g, b);
    }
}
