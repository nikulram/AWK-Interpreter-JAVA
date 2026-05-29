package awkengine.ast;

import awkengine.runtime.ExecutionContext;
import awkengine.runtime.RuntimeSignal;

public record ExitAction(Expression code) implements Statement {
    @Override
    public void execute(ExecutionContext context) {
        throw RuntimeSignal.of(RuntimeSignal.Kind.EXIT);
    }
}
