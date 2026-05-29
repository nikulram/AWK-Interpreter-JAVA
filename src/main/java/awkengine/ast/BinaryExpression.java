package awkengine.ast;

import awkengine.lexer.TokenType;
import awkengine.runtime.ExecutionContext;
import awkengine.runtime.RuntimeValue;

public record BinaryExpression(Expression left, TokenType operator, Expression right) implements Expression {
    @Override
    public RuntimeValue evaluate(ExecutionContext context) {
        RuntimeValue a = left.evaluate(context);
        RuntimeValue b = right.evaluate(context);

        return switch (operator) {
            case PLUS -> {
                if (a.isNumeric() && b.isNumeric()) {
                    yield RuntimeValue.ofNumber(a.asNumber() + b.asNumber());
                }
                yield RuntimeValue.ofString(a.asString() + b.asString());
            }
            case MINUS -> RuntimeValue.ofNumber(a.asNumber() - b.asNumber());
            case STAR -> RuntimeValue.ofNumber(a.asNumber() * b.asNumber());
            case SLASH -> RuntimeValue.ofNumber(a.asNumber() / b.asNumber());
            case PERCENT -> RuntimeValue.ofNumber(a.asNumber() % b.asNumber());
            case CARET -> RuntimeValue.ofNumber(Math.pow(a.asNumber(), b.asNumber()));
            case EQUAL -> RuntimeValue.ofBoolean(a.asString().equals(b.asString()));
            case NOT_EQUAL -> RuntimeValue.ofBoolean(!a.asString().equals(b.asString()));
            case LESS -> RuntimeValue.ofBoolean(a.asNumber() < b.asNumber());
            case LESS_EQUAL -> RuntimeValue.ofBoolean(a.asNumber() <= b.asNumber());
            case GREATER -> RuntimeValue.ofBoolean(a.asNumber() > b.asNumber());
            case GREATER_EQUAL -> RuntimeValue.ofBoolean(a.asNumber() >= b.asNumber());
            case AND -> RuntimeValue.ofBoolean(a.isTruthy() && b.isTruthy());
            case OR -> RuntimeValue.ofBoolean(a.isTruthy() || b.isTruthy());
            default -> throw new IllegalStateException("Unsupported binary operator: " + operator);
        };
    }
}
