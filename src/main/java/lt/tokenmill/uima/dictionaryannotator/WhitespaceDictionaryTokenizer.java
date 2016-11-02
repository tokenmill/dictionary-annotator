package lt.tokenmill.uima.dictionaryannotator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Split dictionary entries by whitespace.
 */
public final class WhitespaceDictionaryTokenizer implements DictionaryTokenizer {

    @Override
    public List<String> tokenize(String text) {
        return text != null ? Arrays.asList(text.split("\\s+")) : new ArrayList<>();
    }
}
