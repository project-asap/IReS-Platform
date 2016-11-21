import cslab.BasicTask;
import java.io.IOException;
import xmlTools.WikiLinksReducer;
import xmlTools.WikiPageLinksMapper;
import xmlTools.XmlInputFormat;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class PreProcessTask extends BasicTask{
        
    
    public PreProcessTask(String args[]) throws IOException {
        super(args);
    }

    PreProcessTask(String input, String output) throws IOException {
        super(input, output);
    }
   
    

    @Override
    public void JobConfig() {
        
        // Input 
        job.setInputFormatClass(XmlInputFormat.class);
        
        // Mapper/Reducer
        job.setMapperClass(         WikiPageLinksMapper.class   );
        job.setCombinerClass(WikiLinksReducer.class);
        job.setReducerClass(WikiLinksReducer.class);
        
        // Intermediate keys
        job.setMapOutputKeyClass(   Text.class                  );
        // Output format
        job.setOutputFormatClass(   TextOutputFormat.class      );
        // Output Keys
        job.setOutputKeyClass(      Text.class                  );
        // Outut Values
        job.setOutputValueClass(    Text.class                  );
        
    }

    @Override
    public String getJobName() {
        return "Processing Wikipedia XML input";
    }

    @Override
    public void envConfig() {
      conf.set(XmlInputFormat.START_TAG_KEY, "<page>");
      conf.set(XmlInputFormat.END_TAG_KEY, "</page>");
    }

    public static void main(String args[]) throws Exception {
        PreProcessTask task = new PreProcessTask(args);
        System.exit( task.run()?0:-1);
    }

}
