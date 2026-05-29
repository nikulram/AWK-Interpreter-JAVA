package awkengine.ast;

import awkengine.runtime.ExecutionContext;

public record DeleteAction(String variableName) implements Statement {
    @Override
    public void execute(ExecutionContext context) {
        context.deleteVariable(variableName);
    }
}
