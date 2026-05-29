package awkengine.ast;

import awkengine.runtime.ExecutionContext;
import awkengine.runtime.RuntimeSignal;

public record ContinueAction() implements Statement {
    @Override
    public void execute(ExecutionContext context) {
        throw RuntimeSignal.of(RuntimeSignal.Kind.CONTINUE);
    }
}
