import cslab.BasicJob;
import java.io.IOException;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.map.TokenCounterMapper;
public class WordCountJob  extends BasicJob{

   private WordCountJob(String[] args) throws IOException {
        super(args);
    }
    
   
  public static void main(String[] args) throws Exception {
    WordCountJob wc = new WordCountJob(args);    
    System.exit(wc.run() ? 0 : 1);
  }

    @Override
    public void JobConfig() {

        job.setJarByClass(Main.class);
        job.setMapperClass(TokenCounterMapper.class);
        job.setCombinerClass(IntSumReducer.class);
        job.setReducerClass(IntSumReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
    }


}
