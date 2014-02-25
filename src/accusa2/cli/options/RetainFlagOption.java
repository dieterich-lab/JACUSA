package accusa2.cli.options;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import accusa2.cli.Parameters;

public class RetainFlagOption extends AbstractACOption {

	public RetainFlagOption(Parameters parameters) {
		super(parameters);
		opt = 'R';
		longOpt = "retain-flags";
	}

	@Override
	public void process(CommandLine line) throws Exception {
		if(line.hasOption(opt)) {
	    	String value = line.getOptionValue(opt);
	    	int retainFlags = Integer.parseInt(value);
	    	if(retainFlags <= 0) {
	    		throw new IllegalArgumentException(longOpt.toUpperCase() + " = " + retainFlags + " not valid.");
	    	}
	    	parameters.setRetainFlags(retainFlags);
	    }
	}

	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		return OptionBuilder.withLongOpt(longOpt)
				.withArgName(longOpt.toUpperCase())
				.hasArg(true)
		        .withDescription("retain reads with SAM flags " + longOpt.toUpperCase() + " \n default: " + parameters.getRetainFlags())
		        .create(opt);
	}

}
