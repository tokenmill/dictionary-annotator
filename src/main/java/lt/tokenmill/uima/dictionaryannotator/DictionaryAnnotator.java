package lt.tokenmill.uima.dictionaryannotator;

import com.opencsv.CSVReader;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import lt.tokenmill.uima.dictionaryannotator.tree.DictionaryTree;
import lt.tokenmill.uima.dictionaryannotator.tree.EntryMetadata;
import lt.tokenmill.uima.dictionaryannotator.tree.TreeMatch;
import lt.tokenmill.uima.dictionaryannotator.tree.TreeMatcher;
import org.apache.commons.io.IOUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@TypeCapability(
        inputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token"})
public class DictionaryAnnotator extends JCasAnnotator_ImplBase {

    /**
     * Tokenizer for dictionary entries. Make sure it is the same as for text.
     */
    public static final String PARAM_TOKENIZER_CLASS = "tokenizerClass";
    @ConfigurationParameter(name = PARAM_TOKENIZER_CLASS, defaultValue = "lt.tokenmill.uima.dictionaryannotator.WhitespaceDictionaryTokenizer")
    private String tokenizerClass;

    /**
     * Mapping between column index in CSV and feature name.
     */
    public static final String PARAM_FEATURE_MAPPING = "featureMapping";
    @ConfigurationParameter(name = PARAM_FEATURE_MAPPING, defaultValue = {}, mandatory = false)
    private String[] featureMapping;
    private Map<Integer, String> featureIndexes;

    /**
     * The annotation to create on matching phases.
     */
    public static final String PARAM_ANNOTATION_TYPE = "annotationType";
    @ConfigurationParameter(name = PARAM_ANNOTATION_TYPE)
    private String annotationType;


    /**
     * The file must contain one entry per line.
     */
    public static final String PARAM_DICTIONARY_LOCATION = "dictionaryLocation";
    @ConfigurationParameter(name = PARAM_DICTIONARY_LOCATION)
    private String dictionaryFile;

    /**
     * Encoding of the dictionary file. Default value - UTF-8
     */
    public static final String PARAM_DICTIONARY_ENCODING = "dictionaryEncoding";
    @ConfigurationParameter(name = PARAM_DICTIONARY_ENCODING, defaultValue = "UTF-8")
    private String dictionaryEncoding;

    /**
     * Is matching case sensitive?. Default value - true
     */
    public static final String PARAM_DICTIONARY_CASE_SENSITIVE = "caseSensitive";
    @ConfigurationParameter(name = PARAM_DICTIONARY_CASE_SENSITIVE, defaultValue = "true")
    private Boolean caseSensitive;

    /**
     * Is matching accent sensitive ("éclair" is not the same as "eclair")?. Default value - true
     */
    public static final String PARAM_DICTIONARY_ACCENT_SENSITIVE = "accentSensitive";
    @ConfigurationParameter(name = PARAM_DICTIONARY_ACCENT_SENSITIVE, defaultValue = "true")
    private Boolean accentSensitive;

    /**
     * Which column in CSV file should be used when matching. Default value - 0
     */
    public static final String PARAM_PHRASE_COLUMN = "phraseColumn";
    @ConfigurationParameter(name = PARAM_PHRASE_COLUMN, defaultValue = "0")
    private Integer phraseColumn;

    private DictionaryTree tree;
    private DictionaryTokenizer tokenizer;
    private TextNormalizer textNormalizer;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        this.tree = new DictionaryTree();
        this.tokenizer = loadTokenizer();
        this.textNormalizer = new TextNormalizer(this.caseSensitive, this.accentSensitive);
        this.featureIndexes = parseFeatureMapping();
        InputStream is = null;
        try {
            URL phraseFileUrl = ResourceUtils.resolveLocation(this.dictionaryFile, context);
            is = phraseFileUrl.openStream();
            CSVReader csvReader = new CSVReader(new InputStreamReader(is, this.dictionaryEncoding));
            for (String[] record : csvReader) {
                String entry = selectEntry(record);
                EntryMetadata metadata = createMetadata(record);
                List<String> tokens = this.tokenizer.tokenize(entry)
                        .stream()
                        .map(textNormalizer::normalize)
                        .collect(Collectors.toList());
                this.tree.addEntry(tokens, metadata);
            }
        } catch (IOException e) {
            throw new ResourceInitializationException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }


    private EntryMetadata createMetadata(String[] record) {
        EntryMetadata entryMetadata = new EntryMetadata();
        entryMetadata.setText(record[this.phraseColumn]);
        entryMetadata.setColumns(record);
        return entryMetadata;
    }

    private String selectEntry(String[] record) {
        return record[this.phraseColumn];
    }

    private DictionaryTokenizer loadTokenizer() {
        try {
            Class<?> tokenizerClazz = Class.forName(this.tokenizerClass);
            return (DictionaryTokenizer) tokenizerClazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load tokenizer '" + this.tokenizerClass + "'", e);
        }
    }

    private Map<Integer,String> parseFeatureMapping() {
        Map<Integer, String> result = new HashMap<>();
        for(String fm : featureMapping) {
            String[] parts = fm.split("\\s*->\\s*");
            result.put(Integer.parseInt(parts[0]), parts[1]);
        }
        return result;
    }

    @Override
    public void process(JCas jcas) throws AnalysisEngineProcessException {
        TreeMatcher treeMatcher = new TreeMatcher(this.tree);
        Iterator<Token> iterator = JCasUtil.iterator(jcas, Token.class);
        Type type = CasUtil.getType(jcas.getCas(), this.annotationType);
        while (iterator.hasNext()) {
            Token token = iterator.next();
            String tokenText = token.getCoveredText();
            tokenText = this.textNormalizer.normalize(tokenText);
            treeMatcher.proceed(token.getBegin(), token.getEnd(), tokenText);
            List<TreeMatch> matches = treeMatcher.getMatches();

            for (TreeMatch match : matches) {
                for (EntryMetadata metadata : match.matchedEntries()) {
                    annotate(jcas, type, match, metadata);
                }
            }
        }
    }

    private void annotate(JCas jcas, Type type, TreeMatch match, EntryMetadata metadata) {
        AnnotationFS annotation =
                jcas.getCas().createAnnotation(type, match.getStart(), match.getEnd());
        String[] columns = metadata.getColumns();
        for (Map.Entry<Integer,String> fi : featureIndexes.entrySet()) {
            if (columns.length > fi.getKey()) {
                Feature feature = type.getFeatureByBaseName(fi.getValue());
                annotation.setFeatureValueFromString(feature, columns[fi.getKey()]);
            }
        }

        jcas.getCas().addFsToIndexes(annotation);
    }
}
