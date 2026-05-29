package awkengine.ast;

import awkengine.runtime.ExecutionContext;
import awkengine.runtime.RuntimeSignal;

public record BreakAction() implements Statement {
    @Override
    public void execute(ExecutionContext context) {
        throw RuntimeSignal.of(RuntimeSignal.Kind.BREAK);
    }
}
