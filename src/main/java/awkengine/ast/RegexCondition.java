package awkengine.ast;

import awkengine.runtime.ExecutionContext;

import java.util.regex.Pattern;

public record RegexCondition(Expression source, Pattern pattern, boolean negated) implements RuleCondition {
    @Override
    public boolean matches(ExecutionContext context) {
        boolean found = pattern.matcher(source.evaluate(context).asString()).find();
        return negated ? !found : found;
    }
}
