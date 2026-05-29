package awkengine.lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AwkScanner {
    private static final Map<String, TokenType> KEYWORDS = new HashMap<>();

    static {
        KEYWORDS.put("BEGIN", TokenType.BEGIN);
        KEYWORDS.put("END", TokenType.END);
        KEYWORDS.put("if", TokenType.IF);
        KEYWORDS.put("else", TokenType.ELSE);
        KEYWORDS.put("while", TokenType.WHILE);
        KEYWORDS.put("for", TokenType.FOR);
        KEYWORDS.put("in", TokenType.IN);
        KEYWORDS.put("print", TokenType.PRINT);
        KEYWORDS.put("printf", TokenType.PRINTF);
        KEYWORDS.put("function", TokenType.FUNCTION);
        KEYWORDS.put("return", TokenType.RETURN);
        KEYWORDS.put("break", TokenType.BREAK);
        KEYWORDS.put("continue", TokenType.CONTINUE);
        KEYWORDS.put("next", TokenType.NEXT);
        KEYWORDS.put("delete", TokenType.DELETE);
        KEYWORDS.put("getline", TokenType.GETLINE);
        KEYWORDS.put("exit", TokenType.EXIT);
    }

    private final String source;
    private final List<LexToken> tokens = new ArrayList<>();
    private int offset;
    private int line = 1;
    private int column = 1;
    private boolean regexExpected = true;

    public AwkScanner(String source) {
        this.source = source == null ? "" : source;
    }

    public List<LexToken> scan() {
        while (!isAtEnd()) {
            char c = peek();

            if (c == ' ' || c == '\t' || c == '\r') {
                advance();
                continue;
            }

            if (c == '\n') {
                SourceLocation location = location();
                advance();
                tokens.add(new LexToken(TokenType.NEWLINE, "\n", location));
                regexExpected = true;
                continue;
            }

            if (c == '#') {
                skipComment();
                continue;
            }

            if (isIdentifierStart(c)) {
                readIdentifier();
                continue;
            }

            if (Character.isDigit(c) || (c == '.' && hasNextDigit())) {
                readNumber();
                continue;
            }

            if (c == '"') {
                readString();
                continue;
            }

            if (c == '/' && regexExpected) {
                readRegex();
                continue;
            }

            readOperatorOrSymbol();
        }

        tokens.add(new LexToken(TokenType.EOF, "", location()));
        return tokens;
    }

    private void readIdentifier() {
        SourceLocation start = location();
        int begin = offset;

        while (!isAtEnd() && isIdentifierPart(peek())) {
            advance();
        }

        String text = source.substring(begin, offset);
        TokenType type = KEYWORDS.getOrDefault(text, TokenType.IDENTIFIER);
        tokens.add(new LexToken(type, text, start));
        regexExpected = false;
    }

    private void readNumber() {
        SourceLocation start = location();
        int begin = offset;

        while (!isAtEnd() && Character.isDigit(peek())) {
            advance();
        }

        if (!isAtEnd() && peek() == '.') {
            advance();
            while (!isAtEnd() && Character.isDigit(peek())) {
                advance();
            }
        }

        tokens.add(new LexToken(TokenType.NUMBER, source.substring(begin, offset), start));
        regexExpected = false;
    }

    private void readString() {
        SourceLocation start = location();
        advance();

        StringBuilder value = new StringBuilder();

        while (!isAtEnd() && peek() != '"') {
            char c = advance();

            if (c == '\\') {
                if (isAtEnd()) {
                    throw new ScanException("Unclosed string literal", start);
                }
                value.append(escape(advance()));
            } else {
                value.append(c);
            }
        }

        if (isAtEnd()) {
            throw new ScanException("Unclosed string literal", start);
        }

        advance();
        tokens.add(new LexToken(TokenType.STRING, value.toString(), start));
        regexExpected = false;
    }

    private void readRegex() {
        SourceLocation start = location();
        advance();

        StringBuilder pattern = new StringBuilder();
        boolean escaped = false;

        while (!isAtEnd()) {
            char c = advance();

            if (escaped) {
                pattern.append(c);
                escaped = false;
            } else if (c == '\\') {
                pattern.append(c);
                escaped = true;
            } else if (c == '/') {
                tokens.add(new LexToken(TokenType.REGEX, pattern.toString(), start));
                regexExpected = false;
                return;
            } else {
                pattern.append(c);
            }
        }

        throw new ScanException("Unclosed regular expression", start);
    }

    private void readOperatorOrSymbol() {
        SourceLocation start = location();
        char c = advance();

        switch (c) {
            case '{' -> emit(TokenType.LEFT_BRACE, "{", start, true);
            case '}' -> emit(TokenType.RIGHT_BRACE, "}", start, false);
            case '(' -> emit(TokenType.LEFT_PAREN, "(", start, true);
            case ')' -> emit(TokenType.RIGHT_PAREN, ")", start, false);
            case '[' -> emit(TokenType.LEFT_BRACKET, "[", start, true);
            case ']' -> emit(TokenType.RIGHT_BRACKET, "]", start, false);
            case ',' -> emit(TokenType.COMMA, ",", start, true);
            case ';' -> emit(TokenType.SEMICOLON, ";", start, true);
            case '?' -> emit(TokenType.QUESTION, "?", start, true);
            case ':' -> emit(TokenType.COLON, ":", start, true);
            case '$' -> emit(TokenType.DOLLAR, "$", start, true);
            case '+' -> {
                if (match('+')) emit(TokenType.INCREMENT, "++", start, false);
                else if (match('=')) emit(TokenType.PLUS_ASSIGN, "+=", start, true);
                else emit(TokenType.PLUS, "+", start, true);
            }
            case '-' -> {
                if (match('-')) emit(TokenType.DECREMENT, "--", start, false);
                else if (match('=')) emit(TokenType.MINUS_ASSIGN, "-=", start, true);
                else emit(TokenType.MINUS, "-", start, true);
            }
            case '*' -> {
                if (match('=')) emit(TokenType.STAR_ASSIGN, "*=", start, true);
                else emit(TokenType.STAR, "*", start, true);
            }
            case '/' -> {
                if (match('=')) emit(TokenType.SLASH_ASSIGN, "/=", start, true);
                else emit(TokenType.SLASH, "/", start, true);
            }
            case '%' -> {
                if (match('=')) emit(TokenType.PERCENT_ASSIGN, "%=", start, true);
                else emit(TokenType.PERCENT, "%", start, true);
            }
            case '^' -> emit(TokenType.CARET, "^", start, true);
            case '=' -> {
                if (match('=')) emit(TokenType.EQUAL, "==", start, true);
                else emit(TokenType.ASSIGN, "=", start, true);
            }
            case '!' -> {
                if (match('=')) emit(TokenType.NOT_EQUAL, "!=", start, true);
                else if (match('~')) emit(TokenType.NOT_MATCH, "!~", start, true);
                else emit(TokenType.NOT, "!", start, true);
            }
            case '<' -> {
                if (match('=')) emit(TokenType.LESS_EQUAL, "<=", start, true);
                else emit(TokenType.LESS, "<", start, true);
            }
            case '>' -> {
                if (match('=')) emit(TokenType.GREATER_EQUAL, ">=", start, true);
                else if (match('>')) emit(TokenType.REDIRECT_APPEND, ">>", start, true);
                else emit(TokenType.GREATER, ">", start, true);
            }
            case '~' -> emit(TokenType.MATCH, "~", start, true);
            case '&' -> {
                if (match('&')) emit(TokenType.AND, "&&", start, true);
                else throw new ScanException("Unexpected '&'", start);
            }
            case '|' -> {
                if (match('|')) emit(TokenType.OR, "||", start, true);
                else emit(TokenType.PIPE, "|", start, true);
            }
            default -> throw new ScanException("Unexpected character '" + c + "'", start);
        }
    }

    private void emit(TokenType type, String text, SourceLocation location, boolean expectsRegexAfter) {
        tokens.add(new LexToken(type, text, location));
        regexExpected = expectsRegexAfter;
    }

    private void skipComment() {
        while (!isAtEnd() && peek() != '\n') {
            advance();
        }
    }

    private char escape(char c) {
        return switch (c) {
            case 'n' -> '\n';
            case 't' -> '\t';
            case 'r' -> '\r';
            case '"' -> '"';
            case '\\' -> '\\';
            default -> c;
        };
    }

    private boolean match(char expected) {
        if (isAtEnd() || peek() != expected) {
            return false;
        }
        advance();
        return true;
    }

    private boolean hasNextDigit() {
        return offset + 1 < source.length() && Character.isDigit(source.charAt(offset + 1));
    }

    private char peek() {
        return source.charAt(offset);
    }

    private char advance() {
        char c = source.charAt(offset++);
        if (c == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }
        return c;
    }

    private SourceLocation location() {
        return new SourceLocation(line, column, offset);
    }

    private boolean isAtEnd() {
        return offset >= source.length();
    }

    private boolean isIdentifierStart(char c) {
        return Character.isLetter(c) || c == '_';
    }

    private boolean isIdentifierPart(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }
}
