OUTPUT="$(find . -name *.java)"

for i in $OUTPUT
do
echo -e '0r lic\nw' | ed $i
done

