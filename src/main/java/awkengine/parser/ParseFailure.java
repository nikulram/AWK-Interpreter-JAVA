package awkengine.parser;

import awkengine.lexer.LexToken;

public class ParseFailure extends RuntimeException {
    public ParseFailure(String message, LexToken token) {
        super(message + " near " + token);
    }
}
