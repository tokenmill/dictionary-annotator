package lt.tokenmill.uima.dictionaryannotator.tree;

import java.util.List;

/**
 * Stores dictionary in a tree structure to allow efficient access.
 */
public class DictionaryTree {

    private DictionaryTreeElement root;

    public DictionaryTree() {
        root = new DictionaryTreeElement(null);
    }

    public void addEntry(List<String> entryTokens, EntryMetadata metadata) {
        DictionaryTreeElement current = root;

        for (String part : entryTokens) {
            current = current.addChild(part);
        }
        current.addMetadata(metadata);
    }

    public DictionaryTreeElement getMatching(String token) {
        return root.getChild(token);
    }
}
