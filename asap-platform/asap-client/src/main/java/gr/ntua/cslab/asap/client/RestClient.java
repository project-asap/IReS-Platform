package gr.ntua.cslab.asap.client;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

public class RestClient {
    
    private ClientConfiguration configuration;

    /**
     * Default constructor
     */
    public RestClient() {
    }
    
    /**
     * Returns the configuration object
     * @return 
     */
    public ClientConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Sets the configuration object.
     * @param configuration 
     */
    public void setConfiguration(ClientConfiguration configuration) {
        this.configuration = configuration;
    }
    
    /**
     * Issues a new Request and returns a string with the response - if  any.
     * @param requestType
     * @param document
     * @param input
     * @return
     * @throws MalformedURLException
     * @throws IOException 
     */
    protected String issueRequest(String requestType, String document, String input) throws MalformedURLException, IOException {
        String urlString = "http://"+configuration.getHost()+":"+configuration.getPort()+"/"+document;
        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        
        con.setRequestMethod(requestType);
        con.setRequestProperty("accept", "application/xml");
        con.setRequestProperty("Content-type", "application/xml");
        
        switch (requestType) {
            case "GET":
                con.setDoInput(true);
                break;
            case "POST":
                con.setDoInput(true);
                con.setDoOutput(true);
                break;
        }
        
        if(input!=null) {
            OutputStream out = con.getOutputStream();
            out.write(input.getBytes());
        }
        
        
        int responseCode = con.getResponseCode();
        Logger.getLogger("Orchestrator client").info("Response code of request is "+responseCode);
        StringBuilder builder = new StringBuilder();
        
        try (InputStream in = con.getInputStream()) {
            byte[] buffer = new byte[1024];
            int count;
            while((count = in.read(buffer))!=-1) {
                builder.append(new String(buffer,0,count));
            }
        }
        return builder.toString();
        
    }
}