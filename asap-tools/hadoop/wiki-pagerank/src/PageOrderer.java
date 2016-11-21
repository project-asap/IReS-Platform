import PRTools.RankingMapper;
import cslab.BasicTask;
import java.io.IOException;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

/**
 *
 * @author cmantas
 */
public class PageOrderer extends BasicTask{

    public PageOrderer(String[] args) throws IOException {
        super(args);
    }

    PageOrderer(String input, String output) throws IOException {
        super(input, output);
    }

    @Override
    public void envConfig() {}

    @Override
    public void JobConfig() {
        job.setOutputKeyClass(FloatWritable.class);
        job.setOutputValueClass(Text.class);
        job.setMapperClass(RankingMapper.class);
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
    }

    @Override
    public String getJobName() {
        return "Ordering of pages by PageRank score";
    }
    
    
    public static void main(String args[]) throws Exception {
         (new PageOrderer(args)).run();
     }
    
}
