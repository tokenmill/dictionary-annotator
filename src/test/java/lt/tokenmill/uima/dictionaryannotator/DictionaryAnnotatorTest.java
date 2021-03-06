package lt.tokenmill.uima.dictionaryannotator;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import lt.tokenmill.uima.dictionaryannotator.type.DictionaryEntry;
import opennlp.uima.tokenize.SimpleTokenizer;
import opennlp.uima.util.UimaUtil;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class DictionaryAnnotatorTest {

    @Test
    public void testFeatureAssignment() throws Exception {
        AnalysisEngineDescription description = AnalysisEngineFactory.createEngineDescription(DictionaryAnnotator.class,
                DictionaryAnnotator.PARAM_DICTIONARY_LOCATION, "classpath:nlproc-dictionary.csv",
                DictionaryAnnotator.PARAM_TOKENIZER_CLASS, SimpleOpenNlpTokenizer.class.getName(),
                DictionaryAnnotator.PARAM_ANNOTATION_TYPE, DictionaryEntry.class.getName(),
                DictionaryAnnotator.PARAM_FEATURE_MAPPING, asList(
                        "1 -> base"));
        JCas jcas = process(description, loadText("wiki-nlproc.txt"));
        Collection<DictionaryEntry> entries = JCasUtil.select(jcas, DictionaryEntry.class);
        assertEquals(8, entries.size());
        assertEquals(Lists.newArrayList("hand-written rules", "machine learning", "Natural language generation",
                "Natural language understanding", "Natural language search"),
                entries.stream().map(DictionaryEntry::getCoveredText).distinct().collect(Collectors.toList()));
        assertEquals(Lists.newArrayList("method", "method", "method", "method", "task", "task", "task", "task"),
                entries.stream().map(DictionaryEntry::getBase).collect(Collectors.toList()));
        assertEquals(Lists.newArrayList(0),
                entries.stream().map(DictionaryEntry::getId).distinct().collect(Collectors.toList()));
    }

    @Test
    public void testCaseInsensitive() throws Exception {
        AnalysisEngineDescription description = AnalysisEngineFactory.createEngineDescription(DictionaryAnnotator.class,
                DictionaryAnnotator.PARAM_DICTIONARY_LOCATION, "classpath:nlproc-dictionary.csv",
                DictionaryAnnotator.PARAM_TOKENIZER_CLASS, SimpleOpenNlpTokenizer.class.getName(),
                DictionaryAnnotator.PARAM_ANNOTATION_TYPE, DictionaryEntry.class.getName(),
                DictionaryAnnotator.PARAM_DICTIONARY_CASE_SENSITIVE, false,
                DictionaryAnnotator.PARAM_FEATURE_MAPPING, asList(
                        "1 -> base", "2 -> id"));
        JCas jcas = process(description, loadText("wiki-nlproc.txt"));
        Collection<DictionaryEntry> entries = JCasUtil.select(jcas, DictionaryEntry.class);
        assertEquals(11, entries.size());
        assertEquals(Lists.newArrayList("Computing Machinery and Intelligence", "hand-written rules",
                "machine learning", "Anaphora resolution",
                "Natural language generation", "Natural language understanding", "Natural language search"),
                entries.stream().map(DictionaryEntry::getCoveredText).distinct().collect(Collectors.toList()));
        assertEquals(Lists.newArrayList("computing machinery", "computing intelligence", "method", "task"),
                entries.stream().map(DictionaryEntry::getBase).distinct().collect(Collectors.toList()));
        assertEquals(Lists.newArrayList(3, 2, 1),
                entries.stream().map(DictionaryEntry::getId).distinct().collect(Collectors.toList()));
    }

    @Test
    public void testAccentInsensitive() throws Exception {
        AnalysisEngineDescription description = AnalysisEngineFactory.createEngineDescription(DictionaryAnnotator.class,
                DictionaryAnnotator.PARAM_DICTIONARY_LOCATION, "classpath:language-dictionary.csv",
                DictionaryAnnotator.PARAM_TOKENIZER_CLASS, SimpleOpenNlpTokenizer.class.getName(),
                DictionaryAnnotator.PARAM_ANNOTATION_TYPE, DictionaryEntry.class.getName(),
                DictionaryAnnotator.PARAM_DICTIONARY_CASE_SENSITIVE, false,
                DictionaryAnnotator.PARAM_DICTIONARY_ACCENT_SENSITIVE, false,
                DictionaryAnnotator.PARAM_FEATURE_MAPPING, asList(
                        "1 -> base"));
        JCas jcas = process(description, loadText("wiki-language-with-accents.txt"));
        Collection<DictionaryEntry> entries = JCasUtil.select(jcas, DictionaryEntry.class);
        assertEquals(8, entries.size());
        assertEquals(Lists.newArrayList("capacité d'exprimer", "lingvistinių ženklų",
                "programmeringsspråk og språk", "gemäß ihrer genetischen"),
                entries.stream().map(DictionaryEntry::getCoveredText).distinct().collect(Collectors.toList()));
        assertEquals(Lists.newArrayList("fr", "fr-no-accents", "lt", "lt-no-accents",
                "no", "no-no-accents", "de", "de-no-accents"),
                entries.stream().map(DictionaryEntry::getBase).distinct().collect(Collectors.toList()));
    }

    @Test
    public void testAccentSensitive() throws Exception {
        AnalysisEngineDescription description = AnalysisEngineFactory.createEngineDescription(DictionaryAnnotator.class,
                DictionaryAnnotator.PARAM_DICTIONARY_LOCATION, "classpath:language-dictionary.csv",
                DictionaryAnnotator.PARAM_TOKENIZER_CLASS, SimpleOpenNlpTokenizer.class.getName(),
                DictionaryAnnotator.PARAM_ANNOTATION_TYPE, DictionaryEntry.class.getName(),
                DictionaryAnnotator.PARAM_DICTIONARY_CASE_SENSITIVE, false,
                DictionaryAnnotator.PARAM_DICTIONARY_ACCENT_SENSITIVE, true,
                DictionaryAnnotator.PARAM_FEATURE_MAPPING, asList(
                        "1 -> base"));
        JCas jcas = process(description, loadText("wiki-language-with-accents.txt"));
        Collection<DictionaryEntry> entries = JCasUtil.select(jcas, DictionaryEntry.class);
        assertEquals(4, entries.size());
        assertEquals(Lists.newArrayList("capacité d'exprimer", "lingvistinių ženklų",
                "programmeringsspråk og språk", "gemäß ihrer genetischen"),
                entries.stream().map(DictionaryEntry::getCoveredText).distinct().collect(Collectors.toList()));
        assertEquals(Lists.newArrayList("fr", "lt", "no", "de"),
                entries.stream().map(DictionaryEntry::getBase).distinct().collect(Collectors.toList()));
    }

    private JCas process(AnalysisEngineDescription dictionaryDescription, String text) {
        try {
            AggregateBuilder builder = new AggregateBuilder();

            builder.add(AnalysisEngineFactory.createEngineDescription(SimpleTokenizer.class,
                    UimaUtil.SENTENCE_TYPE_PARAMETER, "uima.tcas.DocumentAnnotation",
                    UimaUtil.TOKEN_TYPE_PARAMETER, Token.class.getName()));

            builder.add(dictionaryDescription);
            AnalysisEngine engine = AnalysisEngineFactory.createEngine(builder.createAggregateDescription());
            JCas jcas = engine.newJCas();
            jcas.setDocumentText(text);
            engine.process(jcas);
            return jcas;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create UIMA engine", e);
        }
    }

    private static String loadText(String name) {
        try {
            return Resources.toString(Resources.getResource(name), Charsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
