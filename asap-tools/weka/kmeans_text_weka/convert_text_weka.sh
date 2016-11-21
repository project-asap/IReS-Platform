source $(dirname $0)/common.sh
input_dir=$1
output=$2
docs=$3

dir=$(dirname $output)
#create output dir
mkdir -p $dir

virtual_dir="$HOME/Data/docs_virt_dir"

############### creating virt dir  ###################
rm -r $virtual_dir 
mkdir -p $virtual_dir/text/

echo -n  "[PREP] linking $docs documents: "
doc_count=0
for f in $input_dir/*; do
        
	# keep the basename of file
	bn=$(basename $f)

	# if relative path, convert to absolute
	f=$(readlink -f $f)
        ln -s $f $virtual_dir/text/$bn
        ((doc_count+=1))
        if ((doc_count>=docs)); then break;fi

done
echo "OK ($doc_count) "
if ((doc_count<docs)); then
        echo could not find enough docs \(found $doc_count of $docs\). exiting
        exit
fi



echo "STEP 1/3: Text to arff"
java -Xmx15g -cp ${WEKA} weka.core.converters.TextDirectoryLoader \
	     -dir $virtual_dir\
	     > $output

