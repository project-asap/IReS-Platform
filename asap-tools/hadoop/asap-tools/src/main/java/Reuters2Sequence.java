

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mahout_stollen.ChunkedWriter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;


/**
 * Split the Reuters SGML documents into Simple Text files containing: Title, Date, Dateline, Body
 */
public class Reuters2Sequence
{
//    static File reutersDir;
    static File outputDir;
    static ChunkedWriter sequenceWriter;
    
    static int maxArticles = Integer.MAX_VALUE, articlesCount=0, sgmFileCount=0;
    
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    static final Pattern EXTRACTION_PATTERN = Pattern.compile("<TITLE>(.*?)</TITLE>|<DATE>(.*?)</DATE>|<BODY>(.*?)</BODY>");
    private static final String[] META_CHARS = {"&", "<", ">", "\"", "'"};
    private static final String[] META_CHARS_SERIALIZATIONS = {"&amp;", "&lt;", "&gt;", "&quot;", "&apos;"};
    
    static void prepairOutputDir(String outputDirName){
        outputDir=new File(outputDirName);
        File [] files = outputDir.listFiles();
        
        //if there are existing files, delete them
        if(files!=null) {
            System.out.println("Deleting all files in " + outputDir);
            for (File file : files) 
                file.delete();
        }
        
        outputDir.mkdirs();
    }



    static void extract(File reutersDir) throws FileNotFoundException, IOException {
        
        //check if input exists
        if(!reutersDir.exists())
            throw new FileNotFoundException("Input directory "+reutersDir+" does not exist");
        
        
        File [] sgmFiles = reutersDir.listFiles(new FileFilter()
        {
            public boolean accept(File file)
            {
                return file.getName().endsWith(".sgm");
            }
        });
        if (sgmFiles != null && sgmFiles.length > 0)
        {
            for (int i = 0; i < sgmFiles.length; i++)
            {
               sgmFileCount++;
                File sgmFile = sgmFiles[i];
                extractFile(sgmFile);
                 if (articlesCount>=maxArticles) return;
            }
        }
        else
        {
            System.err.println("No .sgm files in " + reutersDir);
        }
    }



    /**
     * Override if you wish to change what is extracted
     *
     * @param sgmFile
     */
    static void extractFile(File sgmFile) throws FileNotFoundException, IOException
    {
            BufferedReader reader = new BufferedReader(new FileReader(sgmFile));

            StringBuffer buffer = new StringBuffer(1024);
            StringBuffer outBuffer = new StringBuffer(1024);

            String line = null;
            int index = -1;
            int docNumber = 0;
            while ((line = reader.readLine()) != null)
            {
                //when we see a closing reuters tag, flush the file

                if ((index = line.indexOf("</REUTERS")) == -1)
                {
                    //Replace the SGM escape sequences

                    buffer.append(line).append(' ');//accumulate the strings for now, then apply regular expression to get the pieces,
                }
                else
                {
                    //Extract the relevant pieces and write to a file in the output dir
                    Matcher matcher = EXTRACTION_PATTERN.matcher(buffer);
                    
                    while (matcher.find() )
                    {
                        for (int i = 1; i <= matcher.groupCount(); i++)
                        {
                            if (matcher.group(i) != null)
                            {
                                outBuffer.append(matcher.group(i));
                            }
                        }
                        outBuffer.append(LINE_SEPARATOR).append(LINE_SEPARATOR);
                    }
                    String out = outBuffer.toString();
                    for (int i = 0; i < META_CHARS_SERIALIZATIONS.length; i++)
                    {
                        out = out.replaceAll(META_CHARS_SERIALIZATIONS[i], META_CHARS[i]);
                    }
                    
                    
                    String outFileName = sgmFile.getName() + "-" + (docNumber++) + ".txt";
                    
                    //System.out.println("Writing " + outFile);
                    handleReutersArticle(outFileName, out);
                    
                    articlesCount++;
                    //if we have reached the requred target, break
                    if (articlesCount>=maxArticles) break;
                    
                    outBuffer.setLength(0);
                    buffer.setLength(0);
                }
            }
            reader.close();
       
    }


    // cmantas
    static void handleReutersArticle(String filename, String contents) throws IOException{
        
        sequenceWriter.write(filename, contents);
        
//        File outFile = new File(outputDir, filename);
//         FileWriter writer = null;
//        try {
//            writer = new FileWriter(outFile);
//            writer.write(contents);
//            writer.close();
//        } catch (IOException ex) {
//            throw new RuntimeException(ex);
//        } finally {
//            try {writer.close();} catch (IOException ex) {}
//        }
    }
    
    public static void main(String[] args) throws IOException
    {
        
        //default values
        int  chunkSize =1984;
        
        for(String a: args) System.out.println(a);
        
        //args parsing
        String localDirPath = args[0];
        String distrSeqDirPath = args[1];
        if(args.length>2)
            if (!args[2].equals("all"))
                maxArticles=Integer.parseInt(args[2]);
        if(args.length>3)
            chunkSize=Integer.parseInt(args[3]);
            
        File localDir = new File(localDirPath);
        Path seqFilePath = new Path(distrSeqDirPath);
        
        Configuration conf = new Configuration();
        
        //delete output files
        (FileSystem.get(conf)).delete(seqFilePath, true);
        
        //init the Writer
        sequenceWriter = new ChunkedWriter(new Configuration(),chunkSize,seqFilePath);
        
        //perform the extraction
        extract(localDir);
        
        //close the writer
        sequenceWriter.close();
        
        
        System.out.println("Read "+ articlesCount + " articles from "+ sgmFileCount + " .sgm files");
            
        }
   

}