package awkengine.runtime;

import awkengine.ast.ActionRule;
import awkengine.ast.DocumentProgram;
import awkengine.ast.Statement;

import java.util.List;

public final class AwkRuntime {
    private final DocumentProgram program;
    private final List<String> input;

    public AwkRuntime(DocumentProgram program, List<String> input) {
        this.program = program;
        this.input = input;
    }

    public OutputBuffer execute() {
        OutputBuffer output = new OutputBuffer();
        ExecutionContext context = new ExecutionContext(output);

        executeAll(program.startupActions(), context);

        for (String line : input) {
            context.loadRecord(line);

            try {
                for (ActionRule rule : program.recordRules()) {
                    if (rule.condition() == null || rule.condition().matches(context)) {
                        executeAll(rule.actions(), context);
                    }
                }
            } catch (RuntimeSignal signal) {
                if (signal.kind() == RuntimeSignal.Kind.NEXT) {
                    continue;
                }
                if (signal.kind() == RuntimeSignal.Kind.EXIT) {
                    return output;
                }
                throw signal;
            }
        }

        try {
            executeAll(program.shutdownActions(), context);
        } catch (RuntimeSignal signal) {
            if (signal.kind() != RuntimeSignal.Kind.EXIT) {
                throw signal;
            }
        }

        return output;
    }

    private void executeAll(List<Statement> statements, ExecutionContext context) {
        for (Statement statement : statements) {
            statement.execute(context);
        }
    }
}
