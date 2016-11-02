package lt.tokenmill.uima.dictionaryannotator.tree;

import java.util.Collections;
import java.util.List;

public class TreeMatch {

    private int start, end;
    private DictionaryTreeElement match;
    private boolean valid = true;

    public TreeMatch(int start, int end, DictionaryTreeElement match) {
        this.start = start;
        this.end = end;
        this.match = match;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public boolean isValid() {
        return valid;
    }

    public List<EntryMetadata> matchedEntries() {
        return match != null ? match.getMetadata() : Collections.EMPTY_LIST;
    }

    public void proceed(int end, String token) {
        if (match != null && match.getChild(token) != null) {
            this.end = end;
            this.match = match.getChild(token);
        } else {
            this.match = null;
            this.valid = false;
        }
    }
}
