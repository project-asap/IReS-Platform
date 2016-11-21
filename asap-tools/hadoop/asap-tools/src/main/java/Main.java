import movers.*;
import static java.util.Arrays.copyOfRange;
import static movers.Mover.move;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;

/**
 *
 * @author cmantas
 */
public class Main {
    
    public static void main(String args[]) throws Exception{
        String command = args[0];
        
        String inputPath = args[1];
        String outputPath = null;
        if(args.length>2)
            outputPath=args[2];       
        
        String [] newArgs =copyOfRange(args, 1, args.length);
        command = command.toLowerCase();
        
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(conf);
        
        MyVectorInput input=null;
        MyVectorOutput output=null;
        
        switch (command){
            case "dir2sequence":
                LocalSeqDirectory.main(newArgs);
                System.exit(0);
                break;
            case "dir2arff":
                MyDir2Arff.main(newArgs);
                System.exit(0);
                break;
            case "reuters2sequence":
                Reuters2Sequence.main(newArgs);
                System.exit(0);
                break;
            case "seqinfo":
                SequenceInfo.main(newArgs);
                System.exit(0);
                break;
            case "loadcsv":
                CSVLoader.main(newArgs);
                System.exit(0);
                break;
            case "arff2mahout":
                input = new ArffInput(inputPath);
                output = new MahoutOutput(outputPath, fs, conf);
                break;
            case "mahout2arff":
                input = new MahoutInput(inputPath,fs,conf);
                output = new ArffOutput(outputPath);
                break;
            case "mahout2spark":
                input = new MahoutInput(inputPath,fs,conf);
                output = new SparkOutput(outputPath,fs,conf);
                break;
            case "arff2spark":
                input = new ArffInput(inputPath);
                output = new SparkOutput(outputPath,fs,conf);
                break;
            case "spark2arff":
                input = new SparkInput(inputPath, fs, conf);
                output = new ArffOutput(outputPath);
                break;
            case "spark2mahout":
                input = new SparkInput(inputPath, fs, conf);
                output = new MahoutOutput(outputPath, fs, conf);
                break;
            default:
                System.err.println("ERROR: I do not know command: "+command);
                System.exit(-1);
        }
        
        //perform the move
        move(input, output);
    }
}
