package awkengine.runtime;

public final class RuntimeValue {
    private final String text;
    private final Double number;

    private RuntimeValue(String text, Double number) {
        this.text = text;
        this.number = number;
    }

    public static RuntimeValue ofString(String text) {
        return new RuntimeValue(text == null ? "" : text, null);
    }

    public static RuntimeValue ofNumber(double number) {
        return new RuntimeValue(format(number), number);
    }

    public static RuntimeValue ofBoolean(boolean value) {
        return new RuntimeValue(value ? "1" : "0", value ? 1.0 : 0.0);
    }

    public boolean isNumeric() {
        if (number != null) {
            return true;
        }

        try {
            Double.parseDouble(text);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    public double asNumber() {
        if (number != null) {
            return number;
        }

        if (text == null || text.isBlank()) {
            return 0.0;
        }

        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException ignored) {
            return 0.0;
        }
    }

    public String asString() {
        return text == null ? "" : text;
    }

    public boolean isTruthy() {
        if (number != null) {
            return number != 0.0;
        }
        return text != null && !text.isEmpty();
    }

    private static String format(double value) {
        if (Math.rint(value) == value) {
            return Long.toString((long) value);
        }
        return Double.toString(value);
    }
}
