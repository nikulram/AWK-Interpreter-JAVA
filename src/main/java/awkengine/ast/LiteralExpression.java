package awkengine.ast;

import awkengine.runtime.ExecutionContext;
import awkengine.runtime.RuntimeValue;

public record LiteralExpression(RuntimeValue value) implements Expression {
    @Override
    public RuntimeValue evaluate(ExecutionContext context) {
        return value;
    }
}
