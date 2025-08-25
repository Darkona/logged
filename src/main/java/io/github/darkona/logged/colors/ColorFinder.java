package io.github.darkona.logged.colors;


import java.util.Arrays;

public class ColorFinder {


    private ColorFinder() {}

    public static ColorEnum findColor(String color) {
        if (color == null) {
            return BasicColor.BLACK;
        }
        if (Arrays.stream(BasicColor.values()).anyMatch(colorEnum -> colorEnum.toString().equals(color)))
            return BasicColor.valueOf(color);
        if (Arrays.stream(Blue.values()).anyMatch(colorEnum -> colorEnum.toString().equals(color)))
            return Blue.valueOf(color);
        if (Arrays.stream(Red.values()).anyMatch(colorEnum -> colorEnum.toString().equals(color)))
            return Red.valueOf(color);
        if (Arrays.stream(Green.values()).anyMatch(colorEnum -> colorEnum.toString().equals(color)))
            return Green.valueOf(color);
        if (Arrays.stream(Yellow.values()).anyMatch(colorEnum -> colorEnum.toString().equals(color)))
            return Yellow.valueOf(color);
        if (Arrays.stream(Orange.values()).anyMatch(colorEnum -> colorEnum.toString().equals(color)))
            return Orange.valueOf(color);
        if (Arrays.stream(Pink.values()).anyMatch(colorEnum -> colorEnum.toString().equals(color)))
            return Pink.valueOf(color);
        if (Arrays.stream(White.values()).anyMatch(colorEnum -> colorEnum.toString().equals(color)))
            return White.valueOf(color);
        if (Arrays.stream(Gray.values()).anyMatch(colorEnum -> colorEnum.toString().equals(color)))
            return Gray.valueOf(color);
        if (Arrays.stream(Brown.values()).anyMatch(colorEnum -> colorEnum.toString().equals(color)))
            return Brown.valueOf(color);
        return BasicColor.BLACK;
    }
}
