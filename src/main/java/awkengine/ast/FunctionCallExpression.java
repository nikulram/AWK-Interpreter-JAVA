package awkengine.ast;

import awkengine.builtin.StandardLibrary;
import awkengine.runtime.ExecutionContext;
import awkengine.runtime.RuntimeValue;

import java.util.ArrayList;
import java.util.List;

public record FunctionCallExpression(String name, List<Expression> arguments) implements Expression {
    @Override
    public RuntimeValue evaluate(ExecutionContext context) {
        List<RuntimeValue> values = new ArrayList<>();

        for (Expression argument : arguments) {
            values.add(argument.evaluate(context));
        }

        return StandardLibrary.call(name, values, context);
    }
}
