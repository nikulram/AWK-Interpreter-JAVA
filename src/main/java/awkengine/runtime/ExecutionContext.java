package awkengine.runtime;

import java.util.HashMap;
import java.util.Map;

public final class ExecutionContext {
    private final Map<String, RuntimeValue> variables = new HashMap<>();
    private final OutputBuffer output;
    private String currentRecord = "";
    private String[] fields = new String[0];

    public ExecutionContext(OutputBuffer output) {
        this.output = output;
        variables.put("FS", RuntimeValue.ofString("\\s+"));
        variables.put("OFS", RuntimeValue.ofString(" "));
        variables.put("NR", RuntimeValue.ofNumber(0));
        variables.put("FNR", RuntimeValue.ofNumber(0));
        variables.put("NF", RuntimeValue.ofNumber(0));
    }

    public void loadRecord(String record) {
        currentRecord = record == null ? "" : record;

        variables.put("NR", RuntimeValue.ofNumber(readVariable("NR").asNumber() + 1));
        variables.put("FNR", RuntimeValue.ofNumber(readVariable("FNR").asNumber() + 1));

        String separator = readVariable("FS").asString();
        if (currentRecord.isBlank()) {
            fields = new String[0];
        } else {
            fields = currentRecord.trim().split(separator);
        }

        variables.put("NF", RuntimeValue.ofNumber(fields.length));
    }

    public RuntimeValue readField(int index) {
        if (index == 0) {
            return RuntimeValue.ofString(currentRecord);
        }

        if (index < 1 || index > fields.length) {
            return RuntimeValue.ofString("");
        }

        return RuntimeValue.ofString(fields[index - 1]);
    }

    public RuntimeValue readVariable(String name) {
        return variables.getOrDefault(name, RuntimeValue.ofString(""));
    }

    public void writeVariable(String name, RuntimeValue value) {
        variables.put(name, value);
    }

    public void deleteVariable(String name) {
        variables.remove(name);
    }

    public void writeLine(String value) {
        output.writeLine(value);
    }

    public void writeRaw(String value) {
        output.writeRaw(value);
    }

    public OutputBuffer output() {
        return output;
    }
}
