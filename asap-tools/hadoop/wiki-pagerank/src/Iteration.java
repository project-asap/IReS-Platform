
import PRTools.RankCalculateMapper;
import PRTools.RankCalculateReduce;
import cslab.BasicTask;
import java.io.IOException;
import org.apache.hadoop.io.Text;

/**
 *
 * @author cmantas
 */
public class Iteration extends BasicTask {

    public Iteration(String[] args) throws IOException {
        super(args);
    }

    Iteration(String input, String output) throws IOException {
        super(input, output);
    }

    @Override
    public void envConfig() {}

    @Override
    public void JobConfig() {
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setMapperClass(RankCalculateMapper.class);
        job.setReducerClass(RankCalculateReduce.class);
    }

    @Override
    public String getJobName() {
        return "Iterating PageRank MR runs";
    }
    
    public static void main(String args[]) throws Exception{
        (new Iteration(args)).run();
    }
    
}
