package awkengine.ast;

import awkengine.runtime.ExecutionContext;

import java.util.List;

public record ConditionalAction(Expression condition, List<Statement> whenTrue, List<Statement> whenFalse) implements Statement {
    @Override
    public void execute(ExecutionContext context) {
        List<Statement> selected = condition.evaluate(context).isTruthy() ? whenTrue : whenFalse;
        for (Statement statement : selected) {
            statement.execute(context);
        }
    }
}
