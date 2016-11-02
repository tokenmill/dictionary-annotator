package lt.tokenmill.uima.dictionaryannotator.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DictionaryTreeElement {

    private String token;

    private List<EntryMetadata> metadata;

    private Map<String, DictionaryTreeElement> children;

    DictionaryTreeElement(String token) {
        this.token = token;
        this.children = new HashMap<>();
        this.metadata = new ArrayList<>();
    }

    DictionaryTreeElement addChild(String token) {
        DictionaryTreeElement child = children.get(token);
        if (child == null) {
            child = new DictionaryTreeElement(token);
            children.put(token, child);
        }
        return child;
    }

    void addMetadata(EntryMetadata metadata) {
        this.metadata.add(metadata);
    }

    public boolean isEndElement() {
        return children.size() > 0;
    }

    public List<EntryMetadata> getMetadata() {
        return this.metadata;
    }

    public DictionaryTreeElement getChild(String token) {
        return children.get(token);
    }
}