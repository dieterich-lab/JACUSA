package jacusa.cli.options;

import jacusa.cli.parameters.SampleParameters;

import java.io.File;
import java.io.FileNotFoundException;

import net.sf.samtools.SAMFileReader;

public class SAMPathnameArg {

	public static final char SEP = ',';

	private int sampleI;
	private SampleParameters parameters;
	
	public SAMPathnameArg(final int sampleI, SampleParameters paramteres) {
		this.sampleI = sampleI;
		this.parameters = paramteres;
	}

	public void processArg(String arg) throws Exception {
		String[] pathnames = arg.split(Character.toString(SEP));
    	for (String pathname : pathnames) {
	    	File file = new File(pathname);
	    	if (! file.exists()) {
	    		throw new FileNotFoundException("File (" + pathname + ") in not accessible!");
	    	}
	    	SAMFileReader reader = new SAMFileReader(file);
	    	if (! reader.hasIndex()) {
	    		reader.close();
	    		throw new FileNotFoundException("Index for BAM file" + sampleI + " is not accessible!");
	    	}
	    	reader.close();
    	}
    	// beware of ugly code
		parameters.setPathnames(pathnames);
	}

}
