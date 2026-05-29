package awkengine.ast;

import awkengine.runtime.ExecutionContext;

public record AssignmentAction(String name, Expression value) implements Statement {
    @Override
    public void execute(ExecutionContext context) {
        context.writeVariable(name, value.evaluate(context));
    }
}
