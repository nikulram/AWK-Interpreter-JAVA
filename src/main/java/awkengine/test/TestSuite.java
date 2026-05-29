package awkengine.test;

import awkengine.ast.DocumentProgram;
import awkengine.lexer.AwkScanner;
import awkengine.lexer.LexToken;
import awkengine.lexer.TokenType;
import awkengine.parser.AwkParser;
import awkengine.runtime.AwkRuntime;

import java.util.ArrayList;
import java.util.List;

public final class TestSuite {
    private final List<String> failures = new ArrayList<>();
    private int assertions;

    public static void main(String[] args) {
        TestSuite suite = new TestSuite();
        suite.runAll();
        suite.report();
    }

    private void runAll() {
        testLexerBasics();
        testLexerKeywordsAndSymbols();
        testLexerStringsRegexAndComments();
        testFieldProcessing();
        testParserAndRuntimeFiltering();
        testArithmeticPrecedence();
        testComparisonAndLogic();
        testConditions();
        testLoops();
        testControlSignals();
        testBuiltIns();
        testFormatting();
        testDefaultActions();
        testRecordCounters();
    }

    private void testLexerBasics() {
        List<LexToken> tokens = scan("$2 >= 80 { print $1, $2 }");

        expect(tokens.get(0).type() == TokenType.DOLLAR, "lexer dollar");
        expect(tokens.get(1).type() == TokenType.NUMBER, "lexer field number");
        expect(tokens.get(2).type() == TokenType.GREATER_EQUAL, "lexer greater equal");
        expect(tokens.get(3).type() == TokenType.NUMBER, "lexer number");
        expect(tokens.get(4).type() == TokenType.LEFT_BRACE, "lexer left brace");
        expect(tokens.get(5).type() == TokenType.PRINT, "lexer print keyword");
        expect(tokens.get(6).type() == TokenType.DOLLAR, "lexer second dollar");
        expect(tokens.get(8).type() == TokenType.COMMA, "lexer comma");
    }

    private void testLexerKeywordsAndSymbols() {
        List<LexToken> tokens = scan("BEGIN { if (x != 0 && y <= 3) print x; } END { exit }");

        expect(tokens.stream().anyMatch(t -> t.type() == TokenType.BEGIN), "keyword BEGIN");
        expect(tokens.stream().anyMatch(t -> t.type() == TokenType.IF), "keyword if");
        expect(tokens.stream().anyMatch(t -> t.type() == TokenType.NOT_EQUAL), "symbol !=");
        expect(tokens.stream().anyMatch(t -> t.type() == TokenType.AND), "symbol &&");
        expect(tokens.stream().anyMatch(t -> t.type() == TokenType.LESS_EQUAL), "symbol <=");
        expect(tokens.stream().anyMatch(t -> t.type() == TokenType.END), "keyword END");
        expect(tokens.stream().anyMatch(t -> t.type() == TokenType.EXIT), "keyword exit");
    }

    private void testLexerStringsRegexAndComments() {
        List<LexToken> tokens = scan("# ignored\n$1 ~ /A.*/ { print \"hit\" }");

        expect(tokens.stream().anyMatch(t -> t.type() == TokenType.REGEX && t.text().equals("A.*")), "lexer regex");
        expect(tokens.stream().anyMatch(t -> t.type() == TokenType.STRING && t.text().equals("hit")), "lexer string");
        expect(tokens.stream().noneMatch(t -> t.text().contains("ignored")), "comment skipped");
    }

    private void testFieldProcessing() {
        expectRun("{ print $0 }", List.of("red blue"), "red blue\n", "field zero");
        expectRun("{ print $1 }", List.of("red blue"), "red\n", "field one");
        expectRun("{ print $2 }", List.of("red blue"), "blue\n", "field two");
        expectRun("{ print NF }", List.of("red blue green"), "3\n", "field count");
    }

    private void testParserAndRuntimeFiltering() {
        String script = "$2 >= 80 { print $1, $2 }";
        List<String> input = List.of("Ava 91", "Ben 72", "Mia 84");

        expectRun(script, input, "Ava 91\nMia 84\n", "runtime filtered print");
    }

    private void testArithmeticPrecedence() {
        expectRun("BEGIN { print 2 + 3 * 4 }", List.of(), "14\n", "multiply before add");
        expectRun("BEGIN { print (2 + 3) * 4 }", List.of(), "20\n", "grouping");
        expectRun("BEGIN { print 2 ^ 3 }", List.of(), "8\n", "power");
        expectRun("BEGIN { print 9 / 3 + 2 }", List.of(), "5\n", "division and add");
        expectRun("BEGIN { print 10 % 4 }", List.of(), "2\n", "modulo");
    }

    private void testComparisonAndLogic() {
        expectRun("BEGIN { print 5 > 3 }", List.of(), "1\n", "greater than");
        expectRun("BEGIN { print 5 < 3 }", List.of(), "0\n", "less than");
        expectRun("BEGIN { print 5 == 5 }", List.of(), "1\n", "equal");
        expectRun("BEGIN { print 5 != 5 }", List.of(), "0\n", "not equal");
        expectRun("BEGIN { print 1 && 1 }", List.of(), "1\n", "logical and");
        expectRun("BEGIN { print 0 || 1 }", List.of(), "1\n", "logical or");
        expectRun("BEGIN { print !1 }", List.of(), "0\n", "logical not");
    }

    private void testConditions() {
        expectRun("BEGIN { if (1) { print \"yes\" } else { print \"no\" } }", List.of(), "yes\n", "if true");
        expectRun("BEGIN { if (0) { print \"yes\" } else { print \"no\" } }", List.of(), "no\n", "if false");
        expectRun("$1 ~ /A/ { print $1 }", List.of("Ava 1", "Ben 2"), "Ava\n", "regex match");
        expectRun("$1 !~ /A/ { print $1 }", List.of("Ava 1", "Ben 2"), "Ben\n", "regex non-match");
    }

    private void testLoops() {
        expectRun(
                "BEGIN { i = 1; while (i <= 3) { print i; i = i + 1 } }",
                List.of(),
                "1\n2\n3\n",
                "while loop"
        );

        expectRun(
                "BEGIN { for (i = 1; i <= 3; i = i + 1) { print i } }",
                List.of(),
                "1\n2\n3\n",
                "for loop"
        );
    }

    private void testControlSignals() {
        expectRun(
                "BEGIN { i = 0; while (1) { i = i + 1; if (i == 3) { break } }; print i }",
                List.of(),
                "3\n",
                "break signal"
        );

        expectRun(
                "BEGIN { for (i = 1; i <= 4; i = i + 1) { if (i == 2) { continue }; print i } }",
                List.of(),
                "1\n3\n4\n",
                "continue signal"
        );

        expectRun(
                "{ if ($1 == \"skip\") { next }; print $1 }",
                List.of("keep 1", "skip 2", "done 3"),
                "keep\ndone\n",
                "next signal"
        );

        expectRun(
                "{ print $1; if ($1 == \"stop\") { exit } } END { print \"after\" }",
                List.of("go", "stop", "never"),
                "go\nstop\n",
                "exit signal"
        );
    }

    private void testBuiltIns() {
        expectRun("BEGIN { print length(\"hello\") }", List.of(), "5\n", "length");
        expectRun("BEGIN { print tolower(\"HeLLo\") }", List.of(), "hello\n", "tolower");
        expectRun("BEGIN { print toupper(\"mix\") }", List.of(), "MIX\n", "toupper");
        expectRun("BEGIN { print substr(\"abcdef\", 2, 3) }", List.of(), "bcd\n", "substr");
        expectRun("BEGIN { print substr(\"abcdef\", 4) }", List.of(), "def\n", "substr tail");
        expectRun("BEGIN { print index(\"abcdef\", \"cd\") }", List.of(), "3\n", "index found");
        expectRun("BEGIN { print index(\"abcdef\", \"zz\") }", List.of(), "0\n", "index missing");
    }

    private void testFormatting() {
        expectRun("BEGIN { printf \"%s:%d\\n\", \"count\", 3 }", List.of(), "count:3\n", "printf");
        expectRun("BEGIN { print sprintf(\"%s-%d\", \"id\", 7) }", List.of(), "id-7\n", "sprintf");
    }

    private void testDefaultActions() {
        expectRun("$2 >= 80", List.of("Ava 91", "Ben 72"), "Ava 91\n", "default print action");
    }

    private void testRecordCounters() {
        expectRun("{ print NR, FNR }", List.of("a", "b", "c"), "1 1\n2 2\n3 3\n", "record counters");
    }

    private List<LexToken> scan(String script) {
        return new AwkScanner(script).scan();
    }

    private void expectRun(String script, List<String> input, String expected, String name) {
        DocumentProgram program = new AwkParser(new AwkScanner(script).scan()).parse();
        String actual = new AwkRuntime(program, input).execute().content().replace("\r\n", "\n");
        expect(expected.equals(actual), name + " expected [" + expected + "] got [" + actual + "]");
    }

    private void expect(boolean condition, String name) {
        assertions++;
        if (!condition) {
            failures.add(name);
        }
    }

    private void report() {
        if (!failures.isEmpty()) {
            for (String failure : failures) {
                System.err.println("failed: " + failure);
            }
            throw new IllegalStateException(failures.size() + " test(s) failed");
        }

        System.out.println("test suite passed: " + assertions + " assertions");
    }
}
