/**
 *
 * @author cmantas
 */
public class Main {
    
    
    public static void main(String args[]) throws Exception{
        
        int iterations=20;
        long preStart, PRStart, OStart;
        
        String input=args[0], output=args[1], taskInput, taskOutput;
        boolean success;
        
        if (args.length>2) iterations = Integer.parseInt(args[2]);
        
        //=========  Preprocess Task =========================================
        taskInput = input;
        taskOutput = output+"/initial";
        System.out.println("[PR]: Preprocess");
        preStart =  System.currentTimeMillis();
        success = (new PreProcessTask(taskInput, taskOutput)).run();
        if(!success) throw new Exception("Preprocessing of xml input failed");
        
        
        //=========  PageRank Iterations' Tasks ==============================
        //iterations
        PRStart =  System.currentTimeMillis();
        System.out.print("[PR]: Itearations ");
        for (int i = 1; i <= iterations; i++) {
            taskInput = taskOutput;    // this iteration will get as input 
                                       // the previous iterations's output 
            taskOutput="iter_"+i;  //The output of this iteration
            System.out.print(i+", ");
            //init and run the itrerative step
            Iteration it=new Iteration(taskInput,taskOutput);
            success=it.run();
            if(!success) throw new Exception("Iteration "+i+" failed");
            it.delete(taskInput);      //delete the previous step's data
        }

        //=========  Ordering the PR vector Task =============================
        //order output
        taskInput=taskOutput;
        System.out.println("\n[PR]: Ordering ");
        OStart= System.currentTimeMillis();
        PageOrderer order = new PageOrderer(taskInput, output);
        success = order.run();
        if(!success) throw new Exception("Ordering of PageRanked pages failed");
        order.delete(taskInput);
        
        
        //========== Report
        System.out.println("Preprocess Time: "+((double)(PRStart - preStart))/1000.0);
        System.out.println("PR Iteration avg Time: "+((double)(OStart-PRStart))/(1000.0*iterations));
        System.out.println("Ordering Time: "+((double)(System.currentTimeMillis()+OStart))/1000.0);
    }
}
