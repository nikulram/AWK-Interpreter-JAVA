package awkengine.ast;

import awkengine.lexer.TokenType;
import awkengine.runtime.ExecutionContext;
import awkengine.runtime.RuntimeValue;

public record UnaryExpression(TokenType operator, Expression value) implements Expression {
    @Override
    public RuntimeValue evaluate(ExecutionContext context) {
        RuntimeValue result = value.evaluate(context);

        return switch (operator) {
            case MINUS -> RuntimeValue.ofNumber(-result.asNumber());
            case NOT -> RuntimeValue.ofBoolean(!result.isTruthy());
            default -> throw new IllegalStateException("Unsupported unary operator: " + operator);
        };
    }
}
