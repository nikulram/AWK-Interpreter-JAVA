package awkengine.builtin;

import awkengine.runtime.ExecutionContext;
import awkengine.runtime.RuntimeValue;

import java.util.List;
import java.util.Locale;

public final class StandardLibrary {
    private StandardLibrary() {
    }

    public static RuntimeValue call(String name, List<RuntimeValue> args, ExecutionContext context) {
        return switch (name) {
            case "length" -> length(args, context);
            case "tolower" -> oneArg(args, name).map(s -> RuntimeValue.ofString(s.toLowerCase(Locale.ROOT)));
            case "toupper" -> oneArg(args, name).map(s -> RuntimeValue.ofString(s.toUpperCase(Locale.ROOT)));
            case "substr" -> substr(args);
            case "index" -> index(args);
            case "sprintf" -> sprintf(args);
            default -> throw new IllegalArgumentException("Unknown function: " + name);
        };
    }

    private static RuntimeValue length(List<RuntimeValue> args, ExecutionContext context) {
        if (args.isEmpty()) {
            return RuntimeValue.ofNumber(context.readField(0).asString().length());
        }

        requireCount(args, 1, "length");
        return RuntimeValue.ofNumber(args.get(0).asString().length());
    }

    private static RuntimeValue substr(List<RuntimeValue> args) {
        if (args.size() < 2 || args.size() > 3) {
            throw new IllegalArgumentException("substr expects two or three arguments");
        }

        String source = args.get(0).asString();
        int start = Math.max(1, (int) args.get(1).asNumber()) - 1;

        if (start >= source.length()) {
            return RuntimeValue.ofString("");
        }

        if (args.size() == 2) {
            return RuntimeValue.ofString(source.substring(start));
        }

        int length = Math.max(0, (int) args.get(2).asNumber());
        int end = Math.min(source.length(), start + length);
        return RuntimeValue.ofString(source.substring(start, end));
    }

    private static RuntimeValue index(List<RuntimeValue> args) {
        requireCount(args, 2, "index");

        String source = args.get(0).asString();
        String target = args.get(1).asString();
        int found = source.indexOf(target);

        return RuntimeValue.ofNumber(found < 0 ? 0 : found + 1);
    }

    private static RuntimeValue sprintf(List<RuntimeValue> args) {
        if (args.isEmpty()) {
            throw new IllegalArgumentException("sprintf expects at least one argument");
        }

        String format = args.get(0).asString();
        Object[] rendered = new Object[Math.max(0, args.size() - 1)];

        for (int i = 1; i < args.size(); i++) {
            RuntimeValue value = args.get(i);
            double numeric = value.asNumber();

            if (value.isNumeric() && Math.rint(numeric) == numeric) {
                rendered[i - 1] = (long) numeric;
            } else if (value.isNumeric()) {
                rendered[i - 1] = numeric;
            } else {
                rendered[i - 1] = value.asString();
            }
        }

        return RuntimeValue.ofString(String.format(format, rendered));
    }

    private static TextArg oneArg(List<RuntimeValue> args, String name) {
        requireCount(args, 1, name);
        return new TextArg(args.get(0).asString());
    }

    private static void requireCount(List<RuntimeValue> args, int expected, String name) {
        if (args.size() != expected) {
            throw new IllegalArgumentException(name + " expects " + expected + " argument(s)");
        }
    }

    private record TextArg(String value) {
        RuntimeValue map(java.util.function.Function<String, RuntimeValue> mapper) {
            return mapper.apply(value);
        }
    }
}
