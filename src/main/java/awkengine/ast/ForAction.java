package awkengine.ast;

import awkengine.runtime.ExecutionContext;
import awkengine.runtime.RuntimeSignal;

import java.util.List;

public record ForAction(Statement initializer, Expression condition, Statement update, List<Statement> body) implements Statement {
    @Override
    public void execute(ExecutionContext context) {
        if (initializer != null) {
            initializer.execute(context);
        }

        while (condition == null || condition.evaluate(context).isTruthy()) {
            try {
                for (Statement statement : body) {
                    statement.execute(context);
                }
            } catch (RuntimeSignal signal) {
                if (signal.kind() == RuntimeSignal.Kind.BREAK) {
                    break;
                }
                if (signal.kind() != RuntimeSignal.Kind.CONTINUE) {
                    throw signal;
                }
            }

            if (update != null) {
                update.execute(context);
            }
        }
    }
}
