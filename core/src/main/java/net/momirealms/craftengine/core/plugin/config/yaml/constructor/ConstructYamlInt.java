package net.momirealms.craftengine.core.plugin.config.yaml.constructor;

import org.snakeyaml.engine.v2.constructor.ConstructScalar;
import org.snakeyaml.engine.v2.exceptions.ConstructorException;
import org.snakeyaml.engine.v2.nodes.Node;

public class ConstructYamlInt extends ConstructScalar {

    @Override
    public Object construct(Node node) {
        String value = constructScalar(node);
        if (value.isEmpty()) {
            throw new ConstructorException("while constructing an int", node.getStartMark(),
                    "found empty value", node.getStartMark());
        }
        return parseInt(value);
    }

    protected int parseInt(String s) {
        int len = s.length();
        int offset = 0;
        int radix = 10;
        boolean negative = false;

        char firstChar = s.charAt(0);
        if (firstChar == '-') {
            negative = true;
            offset = 1;
        } else if (firstChar == '+') {
            offset = 1;
        }

        if (offset < len && s.charAt(offset) == '0' && offset + 1 < len) {
            char nextChar = s.charAt(offset + 1);
            if (nextChar == 'x' || nextChar == 'X') {
                radix = 16;
                offset += 2;
            } else if (nextChar == 'o' || nextChar == 'O') {
                radix = 8;
                offset += 2;
            }
        }

        int result = Integer.parseInt(s, offset, len, radix);
        return negative ? -result : result;
    }
}