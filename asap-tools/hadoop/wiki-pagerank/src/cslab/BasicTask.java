package cslab;
import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.fs.FileSystem;


public abstract class BasicTask { 

  public Job job;
  public Configuration conf;
  String input, output;
  
  private void init(String input, String output) throws IOException{
      this.input=input;
      this.output=output;
      conf = new Configuration();
      envConfig();
      job = Job.getInstance(conf, getJobName());
      JobConfig();
      FileInputFormat.addInputPath(job, new Path(input));
      job.setJarByClass(this.getClass());
  }
  
  public BasicTask(String input, String output) throws IOException{
      init(input, output);
  }
  
  public BasicTask(String args[]) throws IOException{
    if (args.length!=2){
        System.err.println("This Job takes exactly 2 arguments: input, output");
        System.exit(-1);
    }
    else{
        init(args[0], args[1]);
    }
  }
  
  public abstract void envConfig();
  public abstract void JobConfig();
  
  public void delete(String path){
      try {
          (FileSystem.get(conf)).delete(new Path(path), true);
      } catch (IOException ex) { }
  }
  
  
  public abstract String getJobName();
  
  public boolean run() throws IOException, InterruptedException, ClassNotFoundException{
      delete(output);
      FileOutputFormat.setOutputPath(job,new Path(output) );
      return job.waitForCompletion(true);
  }

}
