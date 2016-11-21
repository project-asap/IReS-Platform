echo /usr/local/sbin > bin.list
echo /usr/local/bin >> bin.list
echo /usr/sbin >> bin.list
echo /usr/bin >> bin.list
echo /sbin >> bin.list
echo /bin >> bin.list
echo /usr/games >> bin.list
echo /root/hadoop/bin >> bin.list
echo /root/hbase/bin >> bin.list

#create a new  environment file from bin.list
cat bin.list | paste -s | sed 's/\t/:/g' | sed 's/^/"/g' | sed 's/^/=/g' | sed 's/^/PATH/g' | sed 's/$/"/g' > en

#replace the old environment
mv en /etc/environment
source /etc/environment