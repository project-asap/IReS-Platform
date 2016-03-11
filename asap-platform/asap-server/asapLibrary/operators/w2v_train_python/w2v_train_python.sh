dataset=$1                                                                                                                                     
w2v_model=$2                                                                                                                                
work_dir=.
                                                                                                                                    
echo ======\> Sampling                                                                                                                                         # preprpocess for w2v                                                                                                                       
awk -F';' '{print $4}' $dataset > $work_dir/training_data.txt                                                                                                                         
echo ======\> Training                                                                                                    # run w2v
/root/word2vec/word2vec -train $work_dir/training_data.txt -output $w2v_model -cbow 0 -size 200 -window 10 -negative 0 -hs 1 -sample 1e-5 -threads 2 -binary 1 -iter 5 -min-count 10 
