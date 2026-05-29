package awkengine.ast;

import awkengine.runtime.ExecutionContext;
import awkengine.runtime.RuntimeSignal;
import awkengine.runtime.RuntimeValue;

public record ReturnAction(Expression value) implements Statement {
    @Override
    public void execute(ExecutionContext context) {
        RuntimeValue result = value == null ? RuntimeValue.ofString("") : value.evaluate(context);
        throw RuntimeSignal.returned(result);
    }
}
