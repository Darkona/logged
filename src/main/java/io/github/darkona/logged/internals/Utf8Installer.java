package io.github.darkona.logged.internals;


import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class Utf8Installer {

    public static void install() {

        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
    }

}
