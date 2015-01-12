package jacusa.cli.options;

import jacusa.cli.parameters.AbstractParameters;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

public class BedCoordinatesOption extends AbstractACOption {

	private AbstractParameters parameters;
	
	public BedCoordinatesOption(AbstractParameters parameters) {
		this.parameters = parameters;

		opt = "b";
		longOpt = "bed";
	}

	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		return OptionBuilder.withLongOpt(longOpt)
			.withArgName(longOpt.toUpperCase()) 
			.hasArg(true)
			.withDescription(longOpt.toUpperCase() + " file to scan for variants")
			.create(opt);
	}

	@Override
	public void process(CommandLine line) throws Exception {
		if (line.hasOption(opt)) {
	    	String pathname = line.getOptionValue(opt);
	    	File file = new File(pathname);
	    	if(!file.exists()) {
	    		throw new FileNotFoundException("BED file (" + pathname + ") in not accessible!");
	    	}
    		parameters.setBedPathname(pathname);
	    }
	}

}