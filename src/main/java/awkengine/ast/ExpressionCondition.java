package awkengine.ast;

import awkengine.runtime.ExecutionContext;

public record ExpressionCondition(Expression expression) implements RuleCondition {
    @Override
    public boolean matches(ExecutionContext context) {
        return expression.evaluate(context).isTruthy();
    }
}
