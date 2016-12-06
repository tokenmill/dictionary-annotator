package lt.tokenmill.uima.dictionaryannotator;


import opennlp.tools.tokenize.SimpleTokenizer;

import java.util.Arrays;
import java.util.List;

public class SimpleOpenNlpTokenizer implements DictionaryTokenizer {

    @Override
    public List<String> tokenize(String text) {
        return Arrays.asList(SimpleTokenizer.INSTANCE.tokenize(text));
    }
}
