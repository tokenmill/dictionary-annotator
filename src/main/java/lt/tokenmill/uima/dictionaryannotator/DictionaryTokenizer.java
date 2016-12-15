package lt.tokenmill.uima.dictionaryannotator;

import java.util.List;

public interface DictionaryTokenizer {

    List<String> tokenize(String text);

}
