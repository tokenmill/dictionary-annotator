# dictionary-annotator

Dictionary Annotator is inspired by DKPro's [dictionary-annotator](https://github.com/dkpro/dkpro-core/tree/master/dkpro-core-dictionaryannotator-asl) and UIMA Ruta's [MARKTABLE](https://uima.apache.org/d/ruta-current/tools.ruta.book.html#ugr.tools.ruta.language.actions.marktable) action

## Features

* Annotates JCas with phrases from CSV file (supported by DKPro and MARKTABLE)
* Supports multiple annotations with different features on the same block of text (not supported by DKPro nor MARKTABLE)
* Configurable case sensitivity (supported by MARKTABLE)
* Supports unlimited number of annotation features (supported by MARKTABLE)
* Configurable tokenizer (not supported by DKPro nor MARKTABLE)

## TODO

* Phrase matching using stemmed tokens
* Configurable CSV separator
* Configurable ignored characters (as in MARKTABLE)
