package awkengine.ast;

import awkengine.runtime.ExecutionContext;

public interface RuleCondition {
    boolean matches(ExecutionContext context);
}
