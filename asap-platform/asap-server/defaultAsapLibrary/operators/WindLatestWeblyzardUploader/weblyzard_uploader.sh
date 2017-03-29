for f in $@;
do
    python weblyzard_uploader.py $f -s https://api.weblyzard.com/0.2/observations/weblyzard.com/wind/ -d observation -i $f;
done
