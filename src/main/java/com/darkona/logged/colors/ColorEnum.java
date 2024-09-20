package com.darkona.logged.colors;

public interface ColorEnum {


    static String reset() {
        return "\u001B[0m";
    }

    String toString();

    default String assemble(short r, short g, short b) {
        return "\u001B[38;2;" + r + ";" + g + ";" + b + "m";
    }
}
