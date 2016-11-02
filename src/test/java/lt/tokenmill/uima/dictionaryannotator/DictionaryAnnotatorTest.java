package lt.tokenmill.uima.dictionaryannotator;

import com.google.common.collect.Lists;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import lt.tokenmill.uima.dictionaryannotator.type.DictionaryEntry;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import java.util.Collection;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class DictionaryAnnotatorTest {


    @Test
    public void testFeatureAssignment() throws Exception {
        AnalysisEngineDescription description = AnalysisEngineFactory.createEngineDescription(DictionaryAnnotator.class,
                DictionaryAnnotator.PARAM_DICTIONARY_LOCATION, "classpath:simple-dictionary.csv",
                DictionaryAnnotator.PARAM_ANNOTATION_TYPE, DictionaryEntry.class.getName(),
                DictionaryAnnotator.PARAM_FEATURE_MAPPING, asList(
                        "1 -> base"));
        JCas jcas = process(description, "This is something from dictionary.");
        Collection<DictionaryEntry> entries = JCasUtil.select(jcas, DictionaryEntry.class);
        assertEquals(3, entries.size());
        assertEquals(Lists.newArrayList("is something", "is", "something"),
                entries.stream().map(DictionaryEntry::getCoveredText).collect(Collectors.toList()));
        assertEquals(Lists.newArrayList("something", "be", "something"),
                entries.stream().map(DictionaryEntry::getBase).collect(Collectors.toList()));
    }

    @Test
    public void testCaseInsensitive() throws Exception {
        AnalysisEngineDescription description = AnalysisEngineFactory.createEngineDescription(DictionaryAnnotator.class,
                DictionaryAnnotator.PARAM_DICTIONARY_LOCATION, "classpath:simple-dictionary.csv",
                DictionaryAnnotator.PARAM_ANNOTATION_TYPE, DictionaryEntry.class.getName(),
                DictionaryAnnotator.PARAM_DICTIONARY_CASE_SENSITIVE, false);
        JCas jcas = process(description, "This is something from dictionary.");
        Collection<DictionaryEntry> entries = JCasUtil.select(jcas, DictionaryEntry.class);
        assertEquals(4, entries.size());
        assertEquals(Lists.newArrayList("This is", "is something", "is", "something"),
                entries.stream().map(DictionaryEntry::getCoveredText).collect(Collectors.toList()));
    }

    private JCas process(AnalysisEngineDescription dictionaryDescription, String text) {
        try {
            AggregateBuilder builder = new AggregateBuilder();

            builder.add(AnalysisEngineFactory.createEngineDescription(OpenNlpSegmenter.class,
                    OpenNlpSegmenter.PARAM_VARIANT, "maxent",
                    OpenNlpSegmenter.PARAM_LANGUAGE, "en",
                    OpenNlpSegmenter.PARAM_SEGMENTATION_MODEL_LOCATION,
                    "classpath:/de/tudarmstadt/ukp/dkpro/core/opennlp/lib/sentence-en-maxent.bin",
                    OpenNlpSegmenter.PARAM_TOKENIZATION_MODEL_LOCATION,
                    "classpath:/de/tudarmstadt/ukp/dkpro/core/opennlp/lib/token-en-maxent.bin"));
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
}
