package com.darkona.logged.colors;


public enum White implements ColorEnum {


    WHITE((short) 255, (short) 255, (short) 255),
    SNOW((short) 255, (short) 250, (short) 250),
    HONEYDEW((short) 240, (short) 255, (short) 240),
    MINT_CREAM((short) 245, (short) 255, (short) 250),
    AZURE((short) 240, (short) 255, (short) 255),
    ALICE_BLUE((short) 240, (short) 248, (short) 255),
    GHOST_WHITE((short) 248, (short) 248, (short) 255),
    WHITE_SMOKE((short) 245, (short) 245, (short) 245),
    SEASHELL((short) 255, (short) 245, (short) 238),
    BEIGE((short) 245, (short) 245, (short) 220),
    OLD_LACE((short) 253, (short) 245, (short) 230),
    FLORAL_WHITE((short) 255, (short) 250, (short) 240),
    IVORY((short) 255, (short) 255, (short) 240),
    ANTIQUE_WHITE((short) 250, (short) 235, (short) 215),
    LINEN((short) 250, (short) 240, (short) 230),
    LAVENDER_BLUSH((short) 255, (short) 240, (short) 245),
    MISTY_ROSE((short) 255, (short) 228, (short) 225);


    private final short r;
    private final short g;
    private final short b;


    White(short r, short g, short b) {

        this.r = r;
        this.g = g;
        this.b = b;
    }

    @Override
    public String toString() {
        return assemble(r, g, b);
    }
}
