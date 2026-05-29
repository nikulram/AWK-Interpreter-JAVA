# AWK Interpreter in Java

A structured AWK-style interpreter written in Java. The project includes a scanner, parser, AST model, runtime engine, built-in functions, command-line runner, examples, and a self-test suite.

## Features

- Lexer with token locations
- Whitespace, newline, comment, string, number, identifier, operator, and regex handling
- Parser for BEGIN blocks, END blocks, and pattern-action rules
- AST model for expressions, statements, conditions, and rules
- Runtime context with NR, FNR, NF, FS, OFS, fields, and variables
- Field access using $0, $1, $2, and expression-based field references
- Arithmetic operators: +, -, *, /, %, ^
- Comparison operators: ==, !=, <, <=, >, >=
- Logical operators: &&, ||, !
- Regex match operators: ~ and !~
- print and printf execution
- if and else blocks
- while loops and for loops
- break, continue, next, exit, return, and delete control actions
- Built-in functions including length, substr, index, sprintf, tolower, and toupper
- Example AWK programs
- Self-test and expanded test suite

## Project Structure

src/main/java/awkengine/ast
src/main/java/awkengine/builtin
src/main/java/awkengine/cli
src/main/java/awkengine/lexer
src/main/java/awkengine/parser
src/main/java/awkengine/runtime
src/main/java/awkengine/test
examples

## Requirements

Java 17 or newer.

## Build

mkdir -p out
javac -d out $(find src/main/java -name "*.java")

## Run Demo

java -cp out awkengine.cli.Main examples/demo.awk examples/data.txt

Expected output:

name score
Ava 91
Mia 84
done

## Run Report Example

java -cp out awkengine.cli.Main examples/report.awk examples/report-data.txt

## Run Control Flow Example

java -cp out awkengine.cli.Main examples/control-flow.awk

## Run Tests

java -cp out awkengine.cli.Main --self-test
java -cp out awkengine.test.TestSuite

## License

This project is licensed under the MIT License. You may use, copy, modify, and distribute it freely, but the copyright notice and license text must be included with copies or substantial portions of the software.
