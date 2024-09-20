package com.darkona.logged.colors;


public enum Yellow implements ColorEnum {

    GOLD((short) 255, (short) 215, (short) 0),
    KHAKI((short) 240, (short) 230, (short) 140),
    DARK_KHAKI((short) 189, (short) 183, (short) 107),
    LEMON_CHIFFON((short) 255, (short) 250, (short) 205),
    LIGHT_GOLDENROD_YELLOW((short) 250, (short) 250, (short) 210),
    PAPAYA_WHIP((short) 255, (short) 239, (short) 213),
    MOCCASIN((short) 255, (short) 228, (short) 181),
    PEACH_PUFF((short) 255, (short) 218, (short) 185),
    PALE_GOLDENROD((short) 238, (short) 232, (short) 170),
    YELLOW((short) 255, (short) 255, (short) 0),
    LIGHT_YELLOW((short) 255, (short) 255, (short) 224);

    private final short r;
    private final short g;
    private final short b;

    Yellow(short r, short g, short b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }


    @Override
    public String toString() {
        return assemble(r, g, b);
    }
}
