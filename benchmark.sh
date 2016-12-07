mkdir -p .benchmark

# Download 20 newsgroups

if [ ! -f .benchmark/20news-bydate.tar.gz ]; then
    wget http://qwone.com/~jason/20Newsgroups/20news-bydate.tar.gz -P .benchmark
    tar zxf .benchmark/20news-bydate.tar.gz -C .benchmark
fi

# Run benchmarks
mvn clean test
echo "Benchmarking Dictionary Annotator"
mvn exec:java -Dbenchmark.texts=".benchmark/20news-bydate-test" -Dbenchmark.engine="tokenmill" 2> /dev/null | grep "Benchmark"
echo "Benchmarking Ruta MARKTABLE"
mvn exec:java -Dbenchmark.texts=".benchmark/20news-bydate-test" -Dbenchmark.engine="ruta" 2> /dev/null | grep "Benchmark"
echo "Benchmarking DkPro dictionary-annotator"
mvn exec:java -Dbenchmark.texts=".benchmark/20news-bydate-test" -Dbenchmark.engine="dkpro" 2> /dev/null | grep "Benchmark"

