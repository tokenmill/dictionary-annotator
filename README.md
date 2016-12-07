# dictionary-annotator

Dictionary Annotator is inspired by DKPro's [dictionary-annotator](https://github.com/dkpro/dkpro-core/tree/master/dkpro-core-dictionaryannotator-asl) and UIMA Ruta's [MARKTABLE](https://uima.apache.org/d/ruta-current/tools.ruta.book.html#ugr.tools.ruta.language.actions.marktable) action

## Features

* Annotates JCas with phrases from CSV file (supported by DKPro and MARKTABLE)
* Supports multiple annotations with different features on the same block of text (not supported by DKPro nor MARKTABLE)
* Configurable case sensitivity (supported by MARKTABLE)
* Supports unlimited number of annotation features (supported by MARKTABLE)
* Configurable tokenizer (not supported by DKPro nor MARKTABLE)

## Performance

Simple performance benchmark was done to compare with other alternatives. Numbers are averages from 3 trials.

|| Tokenization               | Time (Tokenization+Dictionary) | Tokens/sec |
|----------------------------|---|--------------------------------|------------|
| DkPro dictionary-annotator | OpenNlp Simple Tokenizer | 368.2 sec                      | 8 724     |
| Ruta MARKTABLE | OpenNlp Simple Tokenizer for dictionary, Ruta tokenizer for texts|21.9 sec | 146 684 |
| **This dictionary annotator** |  OpenNlp Simple Tokenizer |1.7 sec | 1 889 637 |

However this benchmark might be inaccurate because of following differences between annotators:

 * DkPro requires text to be segmented into senteces an tokens. While testing text was marked as single sentence
 * Ruta has its own rich tokenizer which takes significant amount of time

Benchmarking can be done by running ```./benchmark.sh``` 

## Usage

Dictionary (leaders.csv)

```csv
Barack Obama,US,2009-01-20,2017-01-20,president,100023
Dalia Grybauskaite,Lithuania,2009-06-12,,president,100049
Dalia Grybauskaite,EU,2004-11-22,2009-06-01,commissioner,100050

```
Configuration

```java
AnalysisEngineDescription description = AnalysisEngineFactory.createEngineDescription(DictionaryAnnotator.class,
        DictionaryAnnotator.PARAM_DICTIONARY_LOCATION, "classpath:leaders.csv",
        DictionaryAnnotator.PARAM_ANNOTATION_TYPE, Person.class.getName(),
        DictionaryAnnotator.PARAM_DICTIONARY_CASE_SENSITIVE, true,
        DictionaryAnnotator.PARAM_FEATURE_MAPPING, asList(
                "1 -> country", "2 -> from", "3 -> to", "5 -> id", "4 -> role"));
```

Running it on text ```Barack Obama met Dalia Grybauskaite in Vilnius``` would produce 3 annotations:

```
Person(id=100023, from="2009-01-20", to="2017-01-20", country="US", role="president"),
Person(id=100049, from="2009-06-12", to=null, country="Lithuania", role="president"),
Person(id=100050, from="2004-11-22", to="2009-06-01", country="EU", role="commissioner")
```

A working example can be found in [DictionaryAnnotatorTest](https://github.com/tokenmill/dictionary-annotator/blob/master/src/test/java/lt/tokenmill/uima/dictionaryannotator/DictionaryAnnotatorTest.java)

## Configuration

### Basic Example

```java
AnalysisEngineDescription description = AnalysisEngineFactory.createEngineDescription(DictionaryAnnotator.class,
        DictionaryAnnotator.PARAM_DICTIONARY_LOCATION, "classpath:dictionary.csv",
        DictionaryAnnotator.PARAM_ANNOTATION_TYPE, DictionaryEntry.class.getName(),
        DictionaryAnnotator.PARAM_DICTIONARY_CASE_SENSITIVE, false,
        DictionaryAnnotator.PARAM_FEATURE_MAPPING, asList(
                "1 -> feature1", "2 -> feature2"));
```

### Tokenizer

By default whitespace tokenizer is used for dictionary entries tokenization. 
But you can provide a custom one (usually you want your text and dictionary tokenized by the same tokenizer)

```java
AnalysisEngineDescription description = AnalysisEngineFactory.createEngineDescription(DictionaryAnnotator.class,
        DictionaryAnnotator.PARAM_DICTIONARY_LOCATION, "classpath:dictionary.csv",
        DictionaryAnnotator.PARAM_TOKENIZER_CLASS, YourDictionaryTokenizer.class.getName(),
        DictionaryAnnotator.PARAM_ANNOTATION_TYPE, DictionaryEntry.class.getName(),
        DictionaryAnnotator.PARAM_DICTIONARY_CASE_SENSITIVE, false,
        DictionaryAnnotator.PARAM_FEATURE_MAPPING, asList(
                "1 -> feature1", "2 -> feature2"));
```

NOTE: Tokenizer must implement ```lt.tokenmill.uima.dictionaryannotator.DictionaryTokenizer```

### Accent-insensitive matching

Dictionary annotator can match text iggnoring letter accents. To enable this feature set following configuration property to ```true```:

```java
DictionaryAnnotator.PARAM_DICTIONARY_ACCENT_SENSITIVE
```


## TODO

* Phrase matching using stemmed tokens
* Configurable CSV separator
* Configurable ignored characters (as in MARKTABLE)
