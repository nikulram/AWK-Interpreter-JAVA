package awkengine.ast;

import java.util.List;

public record ActionRule(RuleCondition condition, List<Statement> actions) {
}
