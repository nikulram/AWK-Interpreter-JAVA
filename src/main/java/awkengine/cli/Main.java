package awkengine.cli;

import awkengine.ast.DocumentProgram;
import awkengine.lexer.AwkScanner;
import awkengine.lexer.LexToken;
import awkengine.parser.AwkParser;
import awkengine.runtime.AwkRuntime;
import awkengine.runtime.OutputBuffer;
import awkengine.test.SelfTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 1 && args[0].equals("--self-test")) {
            SelfTest.run();
            return;
        }

        if (args.length < 1 || args.length > 2) {
            System.err.println("Usage: java -cp out awkengine.cli.Main <script.awk> [input.txt]");
            System.exit(1);
        }

        String script = Files.readString(Path.of(args[0]));
        List<String> input = args.length == 2 ? Files.readAllLines(Path.of(args[1])) : List.of();

        List<LexToken> tokens = new AwkScanner(script).scan();
        DocumentProgram program = new AwkParser(tokens).parse();
        OutputBuffer output = new AwkRuntime(program, input).execute();

        output.printToConsole();
    }
}
