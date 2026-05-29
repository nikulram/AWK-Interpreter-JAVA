package awkengine.ast;

import awkengine.runtime.ExecutionContext;

import java.util.ArrayList;
import java.util.List;

public record PrintAction(List<Expression> values) implements Statement {
    @Override
    public void execute(ExecutionContext context) {
        if (values.isEmpty()) {
            context.writeLine(context.readField(0).asString());
            return;
        }

        List<String> rendered = new ArrayList<>();
        for (Expression value : values) {
            rendered.add(value.evaluate(context).asString());
        }

        context.writeLine(String.join(context.readVariable("OFS").asString(), rendered));
    }
}
