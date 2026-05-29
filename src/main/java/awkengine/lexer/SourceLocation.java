package awkengine.lexer;

public record SourceLocation(int line, int column, int offset) {
    @Override
    public String toString() {
        return line + ":" + column;
    }
}
