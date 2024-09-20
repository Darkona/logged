package com.darkona.logged;

public interface ColorEnum {


    String toString();

    default String assemble(short r, short g, short b) {
        return "\u001B[38;2;" + r + ";" + g + ";" + b + "m";
    }

    static String reset(){
        return "\u001B[0m";
    }
}
