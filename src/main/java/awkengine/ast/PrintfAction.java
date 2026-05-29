package awkengine.ast;

import awkengine.runtime.ExecutionContext;
import awkengine.runtime.RuntimeValue;

import java.util.List;

public record PrintfAction(List<Expression> values) implements Statement {
    @Override
    public void execute(ExecutionContext context) {
        if (values.isEmpty()) {
            return;
        }

        String format = values.get(0).evaluate(context).asString();
        Object[] rendered = new Object[Math.max(0, values.size() - 1)];

        for (int i = 1; i < values.size(); i++) {
            RuntimeValue value = values.get(i).evaluate(context);
            double numeric = value.asNumber();

            if (value.isNumeric() && Math.rint(numeric) == numeric) {
                rendered[i - 1] = (long) numeric;
            } else if (value.isNumeric()) {
                rendered[i - 1] = numeric;
            } else {
                rendered[i - 1] = value.asString();
            }
        }

        context.writeRaw(String.format(format, rendered));
    }
}
