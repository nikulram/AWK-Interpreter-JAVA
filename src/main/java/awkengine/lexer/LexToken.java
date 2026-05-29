package awkengine.lexer;

public record LexToken(TokenType type, String text, SourceLocation location) {
    public boolean is(TokenType expected) {
        return type == expected;
    }

    @Override
    public String toString() {
        if (text == null || text.isEmpty()) {
            return type + " at " + location;
        }
        return type + "(" + text + ") at " + location;
    }
}
