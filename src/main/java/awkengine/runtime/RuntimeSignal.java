package awkengine.runtime;

public final class RuntimeSignal extends RuntimeException {
    public enum Kind {
        BREAK,
        CONTINUE,
        RETURN,
        NEXT,
        EXIT
    }

    private final Kind kind;
    private final RuntimeValue value;

    private RuntimeSignal(Kind kind, RuntimeValue value) {
        super(kind.toString());
        this.kind = kind;
        this.value = value;
    }

    public static RuntimeSignal of(Kind kind) {
        return new RuntimeSignal(kind, RuntimeValue.ofString(""));
    }

    public static RuntimeSignal returned(RuntimeValue value) {
        return new RuntimeSignal(Kind.RETURN, value);
    }

    public Kind kind() {
        return kind;
    }

    public RuntimeValue value() {
        return value;
    }
}
