source $(dirname $0)/common.sh

if [ "$#" -ne 3 ]; then
    echo "TFIDF needs 3 args: input, output, minDF, got: $#"
    exit
fi
input=$1
output=$2
min_frequency=$3
max_features=999999

echo input $input output $output freq $min_frequency
echo "STEP 2/3: TF/IDF"
java -Xmx15g -cp ${WEKA} weka.filters.unsupervised.attribute.StringToWordVector \
	     -N 0 \
	     -W $max_features \
	     -prune-rate -1.0 \
	     -stemmer weka.core.stemmers.NullStemmer \
	     -M ${min_frequency} \
             -tokenizer "weka.core.tokenizers.WordTokenizer \
	                  -delimiters \" \\r\\n\\t.,;:\\\'\\\"()?\!\$#-0123456789/*%<>@[]+\`~_=&^   \"" \
	     -i $input \
	     -L -S -I -C \
	     -o $output
