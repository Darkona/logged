package com.darkona.logged.colors;


public enum Gray implements ColorEnum {

    GAINSBORO((short) 220, (short) 220, (short) 220),
    LIGHT_GRAY((short) 211, (short) 211, (short) 211),
    SILVER((short) 192, (short) 192, (short) 192),
    DARK_GRAY((short) 169, (short) 169, (short) 169),
    GRAY((short) 128, (short) 128, (short) 128),
    DIM_GRAY((short) 105, (short) 105, (short) 105),
    LIGHT_SLATE_GRAY((short) 119, (short) 136, (short) 153),
    SLATE_GRAY((short) 112, (short) 128, (short) 144),
    DARK_SLATE_GRAY((short) 47, (short) 79, (short) 79),
    BLACK((short) 0, (short) 0, (short) 0);

    private final short r;
    private final short g;
    private final short b;

    Gray(short r, short g, short b) {

        this.r = r;
        this.g = g;
        this.b = b;
    }

    @Override
    public String toString() {
        return assemble(r, g, b);
    }


}
