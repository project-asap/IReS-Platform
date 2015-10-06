package gr.ntua.cslab.asap.client;

public class ClientConfiguration {
    private String host;
    private int port;

    public ClientConfiguration() {
    }
    
    public ClientConfiguration(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
    
    
}
