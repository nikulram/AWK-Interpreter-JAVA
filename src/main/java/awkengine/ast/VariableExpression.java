package awkengine.ast;

import awkengine.runtime.ExecutionContext;
import awkengine.runtime.RuntimeValue;

public record VariableExpression(String name) implements Expression {
    @Override
    public RuntimeValue evaluate(ExecutionContext context) {
        return context.readVariable(name);
    }
}
