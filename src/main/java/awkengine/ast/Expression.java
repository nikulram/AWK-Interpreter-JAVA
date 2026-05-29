package awkengine.ast;

import awkengine.runtime.ExecutionContext;
import awkengine.runtime.RuntimeValue;

public interface Expression {
    RuntimeValue evaluate(ExecutionContext context);
}
