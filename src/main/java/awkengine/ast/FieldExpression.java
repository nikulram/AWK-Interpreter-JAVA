package awkengine.ast;

import awkengine.runtime.ExecutionContext;
import awkengine.runtime.RuntimeValue;

public record FieldExpression(Expression indexExpression) implements Expression {
    @Override
    public RuntimeValue evaluate(ExecutionContext context) {
        int index = (int) indexExpression.evaluate(context).asNumber();
        return context.readField(index);
    }
}
