package awkengine.parser;

import awkengine.ast.ActionRule;
import awkengine.ast.BreakAction;
import awkengine.ast.AssignmentAction;
import awkengine.ast.BinaryExpression;
import awkengine.ast.ConditionalAction;
import awkengine.ast.ContinueAction;
import awkengine.ast.DocumentProgram;
import awkengine.ast.DeleteAction;
import awkengine.ast.Expression;
import awkengine.ast.ExitAction;
import awkengine.ast.ExpressionCondition;
import awkengine.ast.FieldExpression;
import awkengine.ast.ForAction;
import awkengine.ast.FunctionCallExpression;
import awkengine.ast.LiteralExpression;
import awkengine.ast.NextAction;
import awkengine.ast.PrintAction;
import awkengine.ast.PrintfAction;
import awkengine.ast.RegexCondition;
import awkengine.ast.ReturnAction;
import awkengine.ast.RuleCondition;
import awkengine.ast.Statement;
import awkengine.ast.UnaryExpression;
import awkengine.ast.VariableExpression;
import awkengine.ast.WhileAction;
import awkengine.lexer.LexToken;
import awkengine.lexer.TokenType;
import awkengine.runtime.RuntimeValue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class AwkParser {
    private final List<LexToken> tokens;
    private int current;

    public AwkParser(List<LexToken> tokens) {
        this.tokens = tokens;
    }

    public DocumentProgram parse() {
        List<Statement> begin = new ArrayList<>();
        List<ActionRule> rules = new ArrayList<>();
        List<Statement> end = new ArrayList<>();

        skipSeparators();

        while (!check(TokenType.EOF)) {
            if (match(TokenType.BEGIN)) {
                begin.addAll(parseBlock());
            } else if (match(TokenType.END)) {
                end.addAll(parseBlock());
            } else {
                rules.add(parseRule());
            }
            skipSeparators();
        }

        consume(TokenType.EOF, "Expected end of script");
        return new DocumentProgram(begin, rules, end);
    }

    private ActionRule parseRule() {
        RuleCondition condition = null;

        if (!check(TokenType.LEFT_BRACE)) {
            condition = parseCondition();
        }

        List<Statement> actions = check(TokenType.LEFT_BRACE)
                ? parseBlock()
                : List.of(new PrintAction(List.of(new FieldExpression(new LiteralExpression(RuntimeValue.ofNumber(0))))));

        return new ActionRule(condition, actions);
    }

    private RuleCondition parseCondition() {
        Expression source = parseExpression();

        if (match(TokenType.MATCH)) {
            LexToken regex = consume(TokenType.REGEX, "Expected regex after match operator");
            return new RegexCondition(source, Pattern.compile(regex.text()), false);
        }

        if (match(TokenType.NOT_MATCH)) {
            LexToken regex = consume(TokenType.REGEX, "Expected regex after non-match operator");
            return new RegexCondition(source, Pattern.compile(regex.text()), true);
        }

        return new ExpressionCondition(source);
    }

    private List<Statement> parseBlock() {
        consume(TokenType.LEFT_BRACE, "Expected block start");
        skipSeparators();

        List<Statement> statements = new ArrayList<>();

        while (!check(TokenType.RIGHT_BRACE) && !check(TokenType.EOF)) {
            statements.add(parseStatement());
            skipSeparators();
        }

        consume(TokenType.RIGHT_BRACE, "Expected block end");
        return statements;
    }

    private Statement parseStatement() {
        if (match(TokenType.PRINT)) {
            return new PrintAction(parseArguments());
        }

        if (match(TokenType.PRINTF)) {
            return new PrintfAction(parseArguments());
        }

        if (match(TokenType.BREAK)) {
            return new BreakAction();
        }

        if (match(TokenType.CONTINUE)) {
            return new ContinueAction();
        }

        if (match(TokenType.NEXT)) {
            return new NextAction();
        }

        if (match(TokenType.EXIT)) {
            Expression code = isStatementEnd() ? null : parseExpression();
            return new ExitAction(code);
        }

        if (match(TokenType.RETURN)) {
            Expression value = isStatementEnd() ? null : parseExpression();
            return new ReturnAction(value);
        }

        if (match(TokenType.DELETE)) {
            LexToken target = consume(TokenType.IDENTIFIER, "Expected variable name after delete");
            return new DeleteAction(target.text());
        }

        if (match(TokenType.IF)) {
            consume(TokenType.LEFT_PAREN, "Expected '(' after if");
            Expression condition = parseExpression();
            consume(TokenType.RIGHT_PAREN, "Expected ')' after if condition");

            List<Statement> whenTrue = parseBlock();
            List<Statement> whenFalse = match(TokenType.ELSE) ? parseBlock() : List.of();

            return new ConditionalAction(condition, whenTrue, whenFalse);
        }

        if (match(TokenType.WHILE)) {
            consume(TokenType.LEFT_PAREN, "Expected '(' after while");
            Expression condition = parseExpression();
            consume(TokenType.RIGHT_PAREN, "Expected ')' after while condition");
            return new WhileAction(condition, parseBlock());
        }

        if (match(TokenType.FOR)) {
            consume(TokenType.LEFT_PAREN, "Expected '(' after for");

            Statement initializer = null;
            if (!check(TokenType.SEMICOLON)) {
                initializer = parseAssignmentLikeStatement();
            }
            consume(TokenType.SEMICOLON, "Expected ';' after for initializer");

            Expression condition = null;
            if (!check(TokenType.SEMICOLON)) {
                condition = parseExpression();
            }
            consume(TokenType.SEMICOLON, "Expected ';' after for condition");

            Statement update = null;
            if (!check(TokenType.RIGHT_PAREN)) {
                update = parseAssignmentLikeStatement();
            }
            consume(TokenType.RIGHT_PAREN, "Expected ')' after for update");

            return new ForAction(initializer, condition, update, parseBlock());
        }

        if (check(TokenType.IDENTIFIER) && peekNext().is(TokenType.ASSIGN)) {
            return parseAssignmentLikeStatement();
        }

        Expression ignored = parseExpression();
        return context -> ignored.evaluate(context);
    }

    private Statement parseAssignmentLikeStatement() {
        String name = consume(TokenType.IDENTIFIER, "Expected binding target").text();
        consume(TokenType.ASSIGN, "Expected '='");
        Expression value = parseExpression();
        return new AssignmentAction(name, value);
    }

    private List<Expression> parseArguments() {
        List<Expression> values = new ArrayList<>();

        if (isStatementEnd()) {
            return values;
        }

        values.add(parseExpression());

        while (match(TokenType.COMMA)) {
            values.add(parseExpression());
        }

        return values;
    }

    private Expression parseExpression() {
        return parseLogicalOr();
    }

    private Expression parseLogicalOr() {
        Expression expression = parseLogicalAnd();

        while (match(TokenType.OR)) {
            expression = new BinaryExpression(expression, previous().type(), parseLogicalAnd());
        }

        return expression;
    }

    private Expression parseLogicalAnd() {
        Expression expression = parseEquality();

        while (match(TokenType.AND)) {
            expression = new BinaryExpression(expression, previous().type(), parseEquality());
        }

        return expression;
    }

    private Expression parseEquality() {
        Expression expression = parseComparison();

        while (match(TokenType.EQUAL, TokenType.NOT_EQUAL)) {
            expression = new BinaryExpression(expression, previous().type(), parseComparison());
        }

        return expression;
    }

    private Expression parseComparison() {
        Expression expression = parseTerm();

        while (match(TokenType.LESS, TokenType.LESS_EQUAL, TokenType.GREATER, TokenType.GREATER_EQUAL)) {
            expression = new BinaryExpression(expression, previous().type(), parseTerm());
        }

        return expression;
    }

    private Expression parseTerm() {
        Expression expression = parseFactor();

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            expression = new BinaryExpression(expression, previous().type(), parseFactor());
        }

        return expression;
    }

    private Expression parseFactor() {
        Expression expression = parsePower();

        while (match(TokenType.STAR, TokenType.SLASH, TokenType.PERCENT)) {
            expression = new BinaryExpression(expression, previous().type(), parsePower());
        }

        return expression;
    }

    private Expression parsePower() {
        Expression expression = parseUnary();

        while (match(TokenType.CARET)) {
            expression = new BinaryExpression(expression, previous().type(), parseUnary());
        }

        return expression;
    }

    private Expression parseUnary() {
        if (match(TokenType.MINUS, TokenType.NOT)) {
            return new UnaryExpression(previous().type(), parseUnary());
        }

        return parsePrimary();
    }

    private Expression parsePrimary() {
        if (match(TokenType.NUMBER)) {
            String numberText = previous().text();
            return new LiteralExpression(RuntimeValue.ofNumber(Double.parseDouble(numberText)));
        }

        if (match(TokenType.STRING)) {
            String stringText = previous().text();
            return new LiteralExpression(RuntimeValue.ofString(stringText));
        }

        if (match(TokenType.REGEX)) {
            String regexText = previous().text();
            return new LiteralExpression(RuntimeValue.ofString(regexText));
        }

        if (match(TokenType.IDENTIFIER)) {
            String name = previous().text();

            if (match(TokenType.LEFT_PAREN)) {
                List<Expression> args = new ArrayList<>();

                if (!check(TokenType.RIGHT_PAREN)) {
                    args.add(parseExpression());

                    while (match(TokenType.COMMA)) {
                        args.add(parseExpression());
                    }
                }

                consume(TokenType.RIGHT_PAREN, "Expected ')' after function arguments");
                return new FunctionCallExpression(name, args);
            }

            return new VariableExpression(name);
        }

        if (match(TokenType.DOLLAR)) {
            Expression index;
            if (match(TokenType.NUMBER)) {
                index = new LiteralExpression(RuntimeValue.ofNumber(Double.parseDouble(previous().text())));
            } else {
                index = parsePrimary();
            }
            return new FieldExpression(index);
        }

        if (match(TokenType.LEFT_PAREN)) {
            Expression grouped = parseExpression();
            consume(TokenType.RIGHT_PAREN, "Expected ')' after expression");
            return grouped;
        }

        throw error("Expected expression");
    }

    private void skipSeparators() {
        while (match(TokenType.NEWLINE, TokenType.SEMICOLON)) {
        }
    }

    private boolean isStatementEnd() {
        return check(TokenType.NEWLINE)
                || check(TokenType.SEMICOLON)
                || check(TokenType.RIGHT_BRACE)
                || check(TokenType.EOF);
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private LexToken consume(TokenType type, String message) {
        if (check(type)) {
            return advance();
        }

        throw error(message);
    }

    private boolean check(TokenType type) {
        return peek().is(type);
    }

    private LexToken advance() {
        if (!check(TokenType.EOF)) {
            current++;
        }

        return previous();
    }

    private LexToken peek() {
        return tokens.get(current);
    }

    private LexToken peekNext() {
        if (current + 1 >= tokens.size()) {
            return tokens.get(tokens.size() - 1);
        }

        return tokens.get(current + 1);
    }

    private LexToken previous() {
        return tokens.get(current - 1);
    }

    private ParseFailure error(String message) {
        return new ParseFailure(message, peek());
    }
}
