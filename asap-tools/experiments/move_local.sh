src=$1
dst=$2
n=$3
count=0

mkdir -p $dst

for f in $src/*; do
        echo "File --> $f";
	cp $f $dst/;
        ((count++));
        if (("$count" >= $n)) 
          then break 
        fi
done

