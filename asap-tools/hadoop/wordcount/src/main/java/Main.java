import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.mapreduce.lib.map.TokenCounterMapper;
public class Main {

  public static void main(String[] args) throws Exception {
    String input=null, output =null;
    if (args.length!=2){
        System.err.println("This Job takes exactly 2 arguments: input, output");
        System.exit(-1);
    }
    else{
        input=args[0];
        output=args[1];
    }
    
      
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "word count");
    job.setJarByClass(Main.class);
    job.setMapperClass(TokenCounterMapper.class);
    job.setCombinerClass(IntSumReducer.class);
    job.setReducerClass(IntSumReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
    
    FileInputFormat.addInputPath(job, new Path(input));
    Path output_path = new Path(output);
    (FileSystem.get(conf)).delete(output_path, true); //remove previous output
    FileOutputFormat.setOutputPath(job,output_path );
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}