package cslab;

import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.fs.FileSystem;


public abstract class BasicJob {

  public Job job;
  public Configuration conf;
  
  String input, output;
  
  private void init(String input, String output) throws IOException{
      this.input=input;
      this.output=output;
      conf = new Configuration();
      job = Job.getInstance(conf, "word count");
      JobConfig();
      FileInputFormat.addInputPath(job, new Path(input));
  }
  
  public BasicJob(String args[]) throws IOException{
    if (args.length!=2){
        System.err.println("This Job takes exactly 2 arguments: input, output");
        System.exit(-1);
    }
    else{
        init(args[0], args[1]);
    }
  }
  
  public abstract void JobConfig();
  
  public boolean run() throws IOException, InterruptedException, ClassNotFoundException{
      Path output_path = new Path(output);
      try {
          (FileSystem.get(conf)).delete(output_path, true); //remove previous output
      } catch (IOException ex) {}
      FileOutputFormat.setOutputPath(job,output_path );
      return job.waitForCompletion(true);
  }
  
    

}
