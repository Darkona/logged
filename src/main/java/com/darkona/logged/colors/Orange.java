package com.darkona.logged.colors;


public enum Orange implements ColorEnum {

    LIGHT_SALMON((short) 255, (short) 160, (short) 122),
    CORAL((short) 255, (short) 127, (short) 80),
    TOMATO((short) 255, (short) 99, (short) 71),
    ORANGE_RED((short) 255, (short) 69, (short) 0),
    DARK_ORANGE((short) 255, (short) 140, (short) 0),
    ORANGE((short) 255, (short) 165, (short) 0);

    private final short r;
    private final short g;
    private final short b;


    Orange(short r, short g, short b) {

        this.r = r;
        this.g = g;
        this.b = b;
    }

    @Override
    public String toString() {
        return assemble(r, g, b);
    }


}
