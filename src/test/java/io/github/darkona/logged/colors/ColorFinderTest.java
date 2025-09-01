package io.github.darkona.logged.colors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ColorFinderTest {

    @Nested
    @DisplayName("Null / vacío / desconocidos")
    class NullEmptyUnknown {

        @Test
        @DisplayName("null → BasicColor.BLACK")
        void nullReturnsBlack() {
            assertEquals(BasicColor.BLACK, ColorFinder.findColor(null));
        }

        @Test
        @DisplayName("string vacío → BasicColor.BLACK")
        void emptyReturnsBlack() {
            assertEquals(BasicColor.BLACK, ColorFinder.findColor("   "));
        }

        @Test
        @DisplayName("desconocido → BasicColor.BLACK")
        void unknownReturnsBlack() {
            assertEquals(BasicColor.BLACK, ColorFinder.findColor("NOT_A_COLOR"));
        }
    }

    @Nested
    @DisplayName("Match por name() con normalización")
    class NameMatching {

        @ParameterizedTest(name = "''{0}'' → BasicColor.{1}")
        @CsvSource({
                "red,RED",
                "ReD,RED",
                " light gray ,LIGHT_GRAY",
                "light-grey,LIGHT_GREY",
                "CYAN,CYAN",
                "magenta,MAGENTA"
        })
        void basicColorByName(String input, BasicColor expected) {
            assertSame(expected, ColorFinder.findColor(input));
        }

        @Test
        @DisplayName("'white' resuelve a BasicColor.WHITE (no White.WHITE) por precedencia")
        void precedenceWhiteGoesToBasicColor() {
            assertSame(BasicColor.WHITE, ColorFinder.findColor("white"));
        }

        @Test
        @DisplayName("'orange' (no existe en BasicColor) → LogColor.ORANGE")
        void logColorWhenNotInBasic() {
            assertSame(Orange.ORANGE, ColorFinder.findColor("orange"));
        }

        @Test
        @DisplayName("'lavender blush' (con espacio) → White.LAVENDER_BLUSH")
        void whiteFamilyByNameWithSpace() {
            assertSame(White.LAVENDER_BLUSH, ColorFinder.findColor("lavender blush"));
        }
    }

    @Nested
    @DisplayName("Compatibilidad ANSI (toString())")
    class AnsiBackCompat {

        @Test
        @DisplayName("ANSI de BasicColor.RED → BasicColor.RED")
        void ansiOfBasicColorRedResolves() {
            assertSame(BasicColor.RED, ColorFinder.findColor("Red"));
        }

        @Test
        @DisplayName("ANSI de White.SNOW → White.SNOW")
        void ansiOfWhiteSnowResolves() {
            assertSame(White.SNOW, ColorFinder.findColor("snow"));
        }

        @Test
        @DisplayName("ANSI de LogColor.CYAN → LogColor.CYAN")
        void ansiOfLogColorCyanResolves() {

            assertSame(BasicColor.CYAN, ColorFinder.findColor("cYan"));
        }
    }
}
