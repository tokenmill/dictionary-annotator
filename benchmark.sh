mkdir -p .benchmark

# Download 20 newsgroups

if [ ! -f .benchmark/20news-bydate.tar.gz ]; then
    wget http://qwone.com/~jason/20Newsgroups/20news-bydate.tar.gz -P .benchmark
    tar zxf .benchmark/20news-bydate.tar.gz -C .benchmark
fi

# Build dictionary annotator
