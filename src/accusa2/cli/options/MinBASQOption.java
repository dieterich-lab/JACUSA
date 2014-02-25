package accusa2.cli.options;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import accusa2.cli.Parameters;

public class MinBASQOption extends AbstractACOption {

	public MinBASQOption(Parameters parameters) {
		super(parameters);
		opt = 'q';
		longOpt = "min-basq";
	}
	
	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		return OptionBuilder.withLongOpt(longOpt)
			.withArgName(longOpt.toUpperCase())
			.hasArg(true)
	        .withDescription("filter positions with base quality < " + longOpt.toUpperCase() + " \n default: " + parameters.getMinBASQ())
	        .create(opt);
	}

	@Override
	public void process(CommandLine line) throws Exception {
		if(line.hasOption(opt)) {
	    	String value = line.getOptionValue(opt);
	    	byte minBASQ = Byte.parseByte(value);
	    	if(minBASQ < 0) {
	    		throw new IllegalArgumentException(longOpt.toUpperCase() + " = " + minBASQ + " not valid.");
	    	}
	    	parameters.setMinBASQ(minBASQ);
	    }
	}

}
