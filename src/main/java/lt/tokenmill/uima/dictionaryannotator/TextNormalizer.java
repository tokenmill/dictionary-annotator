package lt.tokenmill.uima.dictionaryannotator;

import org.apache.commons.lang3.StringUtils;

/**
 * Normalizes text before matching.
 */
public class TextNormalizer {

    private boolean caseSensitive = true;
    private boolean accentSensitive = true;

    public TextNormalizer() {
    }

    public TextNormalizer(boolean caseSensitive, boolean accentSensitive) {
        this.caseSensitive = caseSensitive;
        this.accentSensitive = accentSensitive;
    }

    public String normalize(String text) {
        text = caseSensitive ? text : text.toLowerCase();
        text = accentSensitive ? text : StringUtils.stripAccents(text);
        return text;
    }
}
