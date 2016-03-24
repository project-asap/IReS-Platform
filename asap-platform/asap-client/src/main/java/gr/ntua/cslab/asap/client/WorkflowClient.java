package gr.ntua.cslab.asap.client;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import gr.ntua.cslab.asap.operators.AbstractOperator;
import gr.ntua.cslab.asap.operators.Operator;
import gr.ntua.cslab.asap.rest.beans.WorkflowDictionary;
import gr.ntua.cslab.asap.workflow.AbstractWorkflow1;

public class WorkflowClient extends RestClient{

    public WorkflowClient() {
        super();

    }

	public void addAbstractWorkflow(AbstractWorkflow1 abstractWorkflow) throws Exception {

        StringWriter writer = new StringWriter();
		JAXBContext jaxbContext = JAXBContext.newInstance( WorkflowDictionary.class );
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, true );
		jaxbMarshaller.marshal( abstractWorkflow.toWorkflowDictionaryRecursive("<br>"), writer );
        String params=writer.toString();

		issueRequest("POST", "abstractWorkflows/add/"+abstractWorkflow.name, params);
	}

	public void removeAbstractWorkflow(String name) throws Exception {
		issueRequest("GET", "abstractWorkflows/remove/"+name, null);
	}

	public String materializeWorkflow(String name, String policy) throws Exception {
		return issueRequest("GET", "abstractWorkflows/materialize/"+name+"?policy="+URLEncoder.encode(policy,"UTF-8"), null);
	}
	
	public String materializeWorkflowWithParameters(String name, String policy, String parameters) throws Exception {
		return issueRequest("GET", "abstractWorkflows/materializeWithParams/"+name+"?policy="+URLEncoder.encode(policy,"UTF-8")
				+"&parameters="+URLEncoder.encode(parameters,"UTF-8"), null);
	}
	
	public WorkflowDictionary getAbstractWorkflowDescription(String name) throws Exception {
		String ret = issueRequest("GET", "abstractWorkflows/XML/"+name, null);
		JAXBContext jaxbContext = JAXBContext.newInstance( WorkflowDictionary.class );
		Unmarshaller u = jaxbContext.createUnmarshaller();
		StringBuffer xmlStr = new StringBuffer( ret );
		WorkflowDictionary wd = (WorkflowDictionary) u.unmarshal( new StreamSource( new StringReader( xmlStr.toString() ) ) );
		return wd;
	}
	
	public WorkflowDictionary getMaterializedWorkflowDescription(String name) throws Exception {
		String ret = issueRequest("GET", "workflows/XML/"+name, null);
		JAXBContext jaxbContext = JAXBContext.newInstance( WorkflowDictionary.class );
		Unmarshaller u = jaxbContext.createUnmarshaller();
		StringBuffer xmlStr = new StringBuffer( ret );
		WorkflowDictionary wd = (WorkflowDictionary) u.unmarshal( new StreamSource( new StringReader( xmlStr.toString() ) ) );
		return wd;
	}
	
	public WorkflowDictionary getRunningWorkflowDescription(String name) throws Exception {
		String ret = issueRequest("GET", "runningWorkflows/XML/"+name, null);
		JAXBContext jaxbContext = JAXBContext.newInstance( WorkflowDictionary.class );
		Unmarshaller u = jaxbContext.createUnmarshaller();
		StringBuffer xmlStr = new StringBuffer( ret );
		WorkflowDictionary wd = (WorkflowDictionary) u.unmarshal( new StreamSource( new StringReader( xmlStr.toString() ) ) );
		return wd;
	}

	public String executeWorkflow(String name) throws Exception {
		return issueRequest("GET", "abstractWorkflows/execute/"+name, null);
	}
	
	public void removeMaterializedWorkflow(String name) throws Exception {
		issueRequest("GET", "workflows/remove/"+URLEncoder.encode(name,"UTF-8"), null);
	}
    
    
	    
}
