package gr.ntua.cslab.asap.client;

import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

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

	public void removeAbstractWorkflow(String name) throws MalformedURLException, IOException {
		issueRequest("GET", "abstractWorkflows/remove/"+name, null);
	}

	public String materializeWorkflow(String name, String policy) throws MalformedURLException, IOException {
		return issueRequest("GET", "abstractWorkflows/materialize/"+name+"?policy="+URLEncoder.encode(policy,"UTF-8"), null);
	}

	public void removeMaterializedWorkflow(String name) throws MalformedURLException, IOException {
		issueRequest("GET", "workflows/remove/"+URLEncoder.encode(name,"UTF-8"), null);
	}
    
    
	    
}
