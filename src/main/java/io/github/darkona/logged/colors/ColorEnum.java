package io.github.darkona.logged.colors;

public interface ColorEnum {

    String RESET = "\u001B[0m";
    static String reset() {
        return "\u001B[0m";
    }

    String toString();

    default String assemble(short r, short g, short b) {
        return "\u001B[38;2;" + r + ";" + g + ";" + b + "m";
    }

    Short red();

    Short green();

    Short blue();
}
