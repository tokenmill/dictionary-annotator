package lt.tokenmill.uima.dictionaryannotator.tree;

public class EntryMetadata {

    private String text;
    private String[] columns;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setColumns(String[] columns) {
        this.columns = columns;
    }

    public String[] getColumns() {
        return columns;
    }
}
