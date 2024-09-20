package com.darkona.logged;

public enum LogColor implements ColorEnum {

    RED((short) 255, (short) 0, (short) 0),
    ORANGE((short) 255, (short) 165, (short) 0),
    YELLOW((short) 255, (short) 255, (short) 0),
    LIME((short) 0, (short) 255, (short) 0),
    GREEN((short) 0, (short) 128, (short) 0),
    CYAN((short) 0, (short) 255, (short) 255),
    BLUE((short) 0, (short) 0, (short) 255),
    PURPLE((short) 128, (short) 0, (short) 128),
    PINK((short) 255, (short) 192, (short) 203),
    MAGENTA((short) 255, (short) 0, (short) 255),
    WHITE((short) 255, (short) 255, (short) 255),
    GRAY((short) 128, (short) 128, (short) 128),
    DARK_GRAY((short) 64, (short) 64, (short) 64),
    BROWN((short) 139, (short) 69, (short) 19),
    GOLD((short) 255, (short) 215, (short) 0),
    BLACK((short) 0, (short) 0, (short) 0);

    private final short r;
    private final short g;
    private final short b;

    LogColor(short r, short g, short b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    @Override
    public String toString() {
        return "\u001B[38;2;" + r + ";" + g + ";" + b + "m";
    }

}
