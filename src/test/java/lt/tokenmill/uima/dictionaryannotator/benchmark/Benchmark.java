package lt.tokenmill.uima.dictionaryannotator.benchmark;

import com.google.common.base.Charsets;
import com.google.common.collect.FluentIterable;
import com.google.common.io.Files;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import lt.tokenmill.uima.dictionaryannotator.DictionaryAnnotator;
import lt.tokenmill.uima.dictionaryannotator.SimpleOpenNlpTokenizer;
import lt.tokenmill.uima.dictionaryannotator.type.DictionaryEntry;
import opennlp.uima.tokenize.SimpleTokenizer;
import opennlp.uima.util.UimaUtil;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.ruta.engine.RutaEngine;

import java.io.File;

public class Benchmark {

    public static void main(String[] args) throws Exception {
        File textDirectory = new File(args[0]);
        FluentIterable<File> files = Files.fileTreeTraverser()
                .breadthFirstTraversal(textDirectory)
                .filter(File::isFile);

        AnalysisEngine engine = createDictionaryAnnotatorEngine();
        //AnalysisEngine engine = createDkProDictionaryAnnotatorEngine();
        //AnalysisEngine engine = createRutaMarktableEngine();
        JCas jcas = engine.newJCas();

        long millis = 0;
        long characters = 0;
        long texts = 0;
        long annotations = 0;

        for (File file : files) {
            String text = Files.asCharSource(file, Charsets.UTF_8).read();
            characters += text.length();
            jcas.setDocumentText(text);
            new Sentence(jcas, 0, text.length()).addToIndexes();
            long start = System.currentTimeMillis();
            engine.process(jcas);
            millis += (System.currentTimeMillis() - start);
            annotations += JCasUtil.select(jcas, DictionaryEntry.class).size();
            texts++;
            jcas.reset();
            if (texts > 0 && texts % 500 == 0) {
                System.out.printf("%d texts, %d annotations, %dK characters, %.2f text/sec, %.1fK chars/sec\n",
                        texts, annotations, characters / 1000, (1000.0 * texts / millis), (1.0 * characters / millis));
            }
        }
        System.out.printf("%d texts, %d annotations, %dK characters, %.2f text/sec, %.1fK chars/sec",
                texts, annotations, characters / 1000, (1000.0 * texts / millis), (1.0 * characters / millis));
    }

    private static AnalysisEngine createDictionaryAnnotatorEngine() throws Exception {
        AggregateBuilder builder = new AggregateBuilder();

        builder.add(AnalysisEngineFactory.createEngineDescription(SimpleTokenizer.class,
                UimaUtil.SENTENCE_TYPE_PARAMETER, Sentence.class.getName(),
                UimaUtil.TOKEN_TYPE_PARAMETER, Token.class.getName()));

        builder.add(AnalysisEngineFactory.createEngineDescription(DictionaryAnnotator.class,
                DictionaryAnnotator.PARAM_DICTIONARY_LOCATION, "classpath:benchmark-dictionary.csv",
                DictionaryAnnotator.PARAM_TOKENIZER_CLASS, SimpleOpenNlpTokenizer.class.getName(),
                DictionaryAnnotator.PARAM_ANNOTATION_TYPE, DictionaryEntry.class.getName(),
                DictionaryAnnotator.PARAM_CSV_SEPARATOR, ";",
                DictionaryAnnotator.PARAM_DICTIONARY_CASE_SENSITIVE, false,
                DictionaryAnnotator.PARAM_DICTIONARY_ACCENT_SENSITIVE, true));
        AnalysisEngine engine = AnalysisEngineFactory.createEngine(builder.createAggregateDescription());
        return engine;
    }

    private static AnalysisEngine createDkProDictionaryAnnotatorEngine() throws Exception {
        AggregateBuilder builder = new AggregateBuilder();

        builder.add(AnalysisEngineFactory.createEngineDescription(OpenNlpSegmenter.class,
                OpenNlpSegmenter.PARAM_VARIANT, "maxent",
                OpenNlpSegmenter.PARAM_LANGUAGE, "en",
                OpenNlpSegmenter.PARAM_SEGMENTATION_MODEL_LOCATION,
                "classpath:/de/tudarmstadt/ukp/dkpro/core/opennlp/lib/sentence-en-maxent.bin",
                OpenNlpSegmenter.PARAM_TOKENIZATION_MODEL_LOCATION,
                "classpath:/de/tudarmstadt/ukp/dkpro/core/opennlp/lib/token-en-maxent.bin"));


        builder.add(AnalysisEngineFactory.createEngineDescription(de.tudarmstadt.ukp.dkpro.core.dictionaryannotator.DictionaryAnnotator.class,
                de.tudarmstadt.ukp.dkpro.core.dictionaryannotator.DictionaryAnnotator.PARAM_MODEL_LOCATION, "classpath:benchmark-dictionary.txt",
                de.tudarmstadt.ukp.dkpro.core.dictionaryannotator.DictionaryAnnotator.PARAM_ANNOTATION_TYPE, DictionaryEntry.class.getName(),
                de.tudarmstadt.ukp.dkpro.core.dictionaryannotator.DictionaryAnnotator.PARAM_VALUE_FEATURE, "base",
                de.tudarmstadt.ukp.dkpro.core.dictionaryannotator.DictionaryAnnotator.PARAM_VALUE, "dummy"));
        AnalysisEngine engine = AnalysisEngineFactory.createEngine(builder.createAggregateDescription());
        return engine;
    }

    private static AnalysisEngine createRutaMarktableEngine() throws Exception {
        AggregateBuilder builder = new AggregateBuilder();

        builder.add(AnalysisEngineFactory.createEngineDescription(AnalysisEngineFactory.createEngineDescription(
                RutaEngine.class,
                RutaEngine.PARAM_STRICT_IMPORTS, true,
                RutaEngine.PARAM_REMOVE_BASICS, true,
                RutaEngine.PARAM_DEBUG, true,
                RutaEngine.PARAM_DEBUG_WITH_MATCHES, true,
                RutaEngine.PARAM_MAIN_SCRIPT, "MarktableBenchmark")));
        AnalysisEngine engine = AnalysisEngineFactory.createEngine(builder.createAggregateDescription());
        return engine;
    }
}
