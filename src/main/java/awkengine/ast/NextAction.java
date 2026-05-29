package awkengine.ast;

import awkengine.runtime.ExecutionContext;
import awkengine.runtime.RuntimeSignal;

public record NextAction() implements Statement {
    @Override
    public void execute(ExecutionContext context) {
        throw RuntimeSignal.of(RuntimeSignal.Kind.NEXT);
    }
}
