package awkengine.test;

import awkengine.ast.DocumentProgram;
import awkengine.lexer.AwkScanner;
import awkengine.parser.AwkParser;
import awkengine.runtime.AwkRuntime;

import java.util.List;

public final class SelfTest {
    private SelfTest() {
    }

    public static void run() {
        check("BEGIN { print \"ready\" }", List.of(), "ready\n");

        check(
                "$2 >= 80 { print $1, $2 }",
                List.of("Ava 91", "Ben 72", "Mia 84", "Leo 66"),
                "Ava 91\nMia 84\n"
        );

        check(
                "BEGIN { total = 2 + 3 * 4; print total }",
                List.of(),
                "14\n"
        );

        check(
                "$1 ~ /A/ { print $1 }",
                List.of("Ava 91", "Ben 72"),
                "Ava\n"
        );

        check(
                "BEGIN { if (1) { print \"yes\" } else { print \"no\" } }",
                List.of(),
                "yes\n"
        );

        check(
                "BEGIN { printf \"%s:%d\\n\", \"count\", 3 }",
                List.of(),
                "count:3\n"
        );

        check(
                "BEGIN { print toupper(substr(\"abcdef\", 2, 3)) }",
                List.of(),
                "BCD\n"
        );

        System.out.println("self tests passed");
    }

    private static void check(String script, List<String> input, String expected) {
        DocumentProgram program = new AwkParser(new AwkScanner(script).scan()).parse();
        String actual = new AwkRuntime(program, input).execute().content().replace("\r\n", "\n");

        if (!expected.equals(actual)) {
            throw new IllegalStateException("Expected [" + expected + "] but got [" + actual + "]");
        }
    }
}
