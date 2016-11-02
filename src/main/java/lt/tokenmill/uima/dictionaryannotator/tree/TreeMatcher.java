package lt.tokenmill.uima.dictionaryannotator.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TreeMatcher {

    private List<TreeMatch> matches = new ArrayList<>();
    private DictionaryTree tree;

    public TreeMatcher(DictionaryTree tree) {
        this.tree = tree;
    }

    public void proceed(int begin, int end, String token) {
        for (TreeMatch match : matches) {
            match.proceed(end, token);
        }
        DictionaryTreeElement matched = tree.getMatching(token);
        matches.add(new TreeMatch(begin, end, matched));
        matches = matches.stream()
                .filter(TreeMatch::isValid)
                .collect(Collectors.toList());
    }

    public List<TreeMatch> getMatches() {
        return matches.stream()
                .filter(TreeMatch::isValid)
                .filter(tm -> !tm.matchedEntries().isEmpty())
                .collect(Collectors.toList());
    }
}
