package awkengine.runtime;

public final class OutputBuffer {
    private final StringBuilder builder = new StringBuilder();

    public void writeLine(String value) {
        builder.append(value).append(System.lineSeparator());
    }

    public void writeRaw(String value) {
        builder.append(value);
    }

    public String content() {
        return builder.toString();
    }

    public void printToConsole() {
        System.out.print(builder);
    }
}
