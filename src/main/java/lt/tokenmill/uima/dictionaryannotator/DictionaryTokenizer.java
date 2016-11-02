package lt.tokenmill.uima.dictionaryannotator;

import java.util.List;

interface DictionaryTokenizer {

    List<String> tokenize(String text);

}
