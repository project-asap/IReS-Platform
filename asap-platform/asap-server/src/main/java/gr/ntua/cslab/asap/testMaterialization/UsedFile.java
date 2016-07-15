package gr.ntua.cslab.asap.testMaterialization;

public class UsedFile {
	private String filename;
	
	/*
	 * type = 0 if is input file
	 * type = 1 if is output file
	 */
	private boolean outputfile;
	
	private long size;

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public boolean isOutputfile() {
		return outputfile;
	}

	public void setOutputfile(boolean outputfile) {
		this.outputfile = outputfile;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}
	
	@Override
	public String toString() {
		return "UsedFile [filename=" + filename + ", outputfile=" + outputfile + ", size=" + size + "]";
	}
	
}
