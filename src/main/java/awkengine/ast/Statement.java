package awkengine.ast;

import awkengine.runtime.ExecutionContext;

public interface Statement {
    void execute(ExecutionContext context);
}
