package awkengine.ast;

import java.util.List;

public record DocumentProgram(
        List<Statement> startupActions,
        List<ActionRule> recordRules,
        List<Statement> shutdownActions
) {
}
