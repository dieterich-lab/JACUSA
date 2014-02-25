package accusa2.cli.options;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import accusa2.cli.Parameters;

public class PileupBuilderOption extends AbstractACOption {

	private final char STRAND_SPECIFIC 	= 'S';
	private final char STRAND_UNSPECIFIC 	= 'U';
	private final char SEP 				= ',';
	
	public PileupBuilderOption(Parameters parameters) {
		super(parameters);
		opt = 'P';
		longOpt = "build-pileup";
	}
	
	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		return OptionBuilder.withLongOpt(longOpt)
			.withArgName(longOpt.toUpperCase())
			.hasArg(true)
	        .withDescription("Choose how parallel pileups are build: strand specific (" + STRAND_SPECIFIC + ") or strand unspecific (" + STRAND_UNSPECIFIC + ")\n default: " + STRAND_UNSPECIFIC + SEP + STRAND_UNSPECIFIC)
	        .create(opt);
	}

	@Override
	public void process(CommandLine line) throws Exception {
		if(line.hasOption(opt)) {
	    	char[] value = line.getOptionValue(opt).toCharArray();
	    	if(value.length != 3) {
	    		throw new IllegalArgumentException("Possible values for " + longOpt.toUpperCase() + ": S,S or U,U or S,U or U,S");
	    	}
	    	parameters.setIsDirected1(parse(value[0]));
	    	parameters.setIsDirected2(parse(value[2]));
	    }
	}

	private boolean parse(char c) {
		switch(c) {
		case STRAND_SPECIFIC:
			return true;

		case STRAND_UNSPECIFIC:
			return false;
			
		default:
			throw new IllegalArgumentException("Possible values for " + longOpt.toUpperCase() + ": S,S or U,U or S,U or U,S");
		}
	}
	
}
