iterations=50

hadoop jar target/*.jar Main input/wikipedia output/wiki_pagerank $iterations 2> pagerank_stderr.out 
echo "===== TAIL  ====="
hcat output/wiki_pagerank/part* | tail -n 100
echo "===== HEAD  ====="
hcat output/wiki_pagerank/part* | head -n 100
