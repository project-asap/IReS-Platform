package gr.ntua.cslab.asap.testMaterialization;

import java.util.Hashtable;

public interface XMLReader {
	
	public Hashtable<Integer, Object> readData ();

	public Hashtable<Integer, Object> readData(Hashtable<Integer, Object> nodes);
}
