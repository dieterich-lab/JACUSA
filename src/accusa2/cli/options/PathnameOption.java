package accusa2.cli.options;

import java.io.File;
import java.io.FileNotFoundException;

import net.sf.samtools.SAMFileReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;

import accusa2.cli.parameters.SampleParameters;

public class PathnameOption extends AbstractACOption {

	public static final char SEP = ',';

	private char c;
	private SampleParameters parameters;
	
	// TODO make two classes of this
	public PathnameOption(char c, SampleParameters paramteres) {
		this.c = c;
		this.parameters = paramteres;

		opt = c;
		longOpt = "bam" + c;
	}

	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		return OptionBuilder.withLongOpt(longOpt)
			.withArgName(longOpt.toUpperCase()) 
			.hasArg(true)
			.withDescription("Path to file " + longOpt.toUpperCase())
			.create(opt);
	}

	@Override
	public void process(CommandLine line) throws Exception {
		if (line.hasOption(c)) {
			String[] pathnames = line.getOptionValue(c).split(Character.toString(SEP));
	    	for (String pathname : pathnames) {
		    	File file = new File(pathname);
		    	if (! file.exists()) {
		    		throw new FileNotFoundException("File " + longOpt.toUpperCase() + " (" + pathname + ") in not accessible!");
		    	}
		    	SAMFileReader reader = new SAMFileReader(file);
		    	if (! reader.hasIndex()) {
		    		reader.close();
		    		throw new FileNotFoundException("Index for BAM file" + c + " is not accessible!");
		    	}
		    	reader.close();
	    	}
	    	// beware of ugly code
    		parameters.setPathnames(pathnames);
	    } else {
	    	throw new ParseException("BAM File" + c + " is not provided!");
	    }
	}

}
