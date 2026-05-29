package awkengine.ast;

import awkengine.runtime.ExecutionContext;
import awkengine.runtime.RuntimeSignal;

import java.util.List;

public record WhileAction(Expression condition, List<Statement> body) implements Statement {
    @Override
    public void execute(ExecutionContext context) {
        while (condition.evaluate(context).isTruthy()) {
            try {
                for (Statement statement : body) {
                    statement.execute(context);
                }
            } catch (RuntimeSignal signal) {
                if (signal.kind() == RuntimeSignal.Kind.BREAK) {
                    break;
                }
                if (signal.kind() == RuntimeSignal.Kind.CONTINUE) {
                    continue;
                }
                throw signal;
            }
        }
    }
}
