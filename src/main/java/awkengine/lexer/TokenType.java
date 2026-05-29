package awkengine.lexer;

public enum TokenType {
    IDENTIFIER,
    NUMBER,
    STRING,
    REGEX,

    BEGIN,
    END,
    IF,
    ELSE,
    WHILE,
    FOR,
    IN,
    PRINT,
    PRINTF,
    FUNCTION,
    RETURN,
    BREAK,
    CONTINUE,
    NEXT,
    DELETE,
    GETLINE,
    EXIT,

    LEFT_BRACE,
    RIGHT_BRACE,
    LEFT_PAREN,
    RIGHT_PAREN,
    LEFT_BRACKET,
    RIGHT_BRACKET,

    COMMA,
    SEMICOLON,
    QUESTION,
    COLON,
    DOLLAR,

    ASSIGN,
    PLUS,
    MINUS,
    STAR,
    SLASH,
    PERCENT,
    CARET,

    PLUS_ASSIGN,
    MINUS_ASSIGN,
    STAR_ASSIGN,
    SLASH_ASSIGN,
    PERCENT_ASSIGN,

    INCREMENT,
    DECREMENT,

    EQUAL,
    NOT_EQUAL,
    LESS,
    LESS_EQUAL,
    GREATER,
    GREATER_EQUAL,

    MATCH,
    NOT_MATCH,
    AND,
    OR,
    NOT,

    PIPE,
    REDIRECT_OUT,
    REDIRECT_APPEND,

    NEWLINE,
    EOF
}
