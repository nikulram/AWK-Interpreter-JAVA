package awkengine.lexer;

public class ScanException extends RuntimeException {
    public ScanException(String message, SourceLocation location) {
        super(message + " at " + location);
    }
}
