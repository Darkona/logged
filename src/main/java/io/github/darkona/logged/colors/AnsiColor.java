package io.github.darkona.logged.colors;


public enum AnsiColor implements ColorEnum {

    BLACK((short) 30),
    RED((short) 31),
    GREEN((short) 32),
    YELLOW((short) 33),
    BLUE((short) 34),
    MAGENTA((short) 35),
    CYAN((short) 36),
    GRAY((short) 37),
    LIGHT_GRAY((short) 90),
    LIGHT_RED((short) 91),
    LIGHT_GREEN((short) 92),
    LIGHT_YELLOW((short) 93),
    LIGHT_BLUE((short) 94),
    LIGHT_MAGENTA((short) 95),
    LIGHT_CYAN((short) 96),
    WHITE((short) 97),
    LIGHT_GREY((short) 90),
    GREY((short) 37);

    private final short ansi;

    AnsiColor(short ansi) {
        this.ansi = ansi;
    }

    @Override
    public String toString() {
        return "\u001B[0;" + ansi + "m";
    }

    @Override
    public Short red() {
        return 0;
    }

    @Override
    public Short green() {
        return 0;
    }

    @Override
    public Short blue() {
        return 0;
    }

    public String apply(String text) {
        return "\u001B[0;" + ansi + "m" + text + "\u001B[0m";
    }
}
