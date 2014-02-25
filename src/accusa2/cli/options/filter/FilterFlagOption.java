package accusa2.cli.options.filter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import accusa2.cli.Parameters;
import accusa2.cli.options.AbstractACOption;

public class FilterFlagOption extends AbstractACOption {

	public FilterFlagOption(Parameters parameters) {
		super(parameters);
		opt = 'F';
		longOpt = "filter-flags";
	}

	@Override
	public void process(CommandLine line) throws Exception {
		if(line.hasOption(opt)) {
	    	String value = line.getOptionValue(opt);
	    	int filterFlags = Integer.parseInt(value);
	    	if(filterFlags <= 0) {
	    		throw new IllegalArgumentException(longOpt.toUpperCase() + " = " + filterFlags + " not valid.");
	    	}
	    	parameters.setFilterFlags(filterFlags);
	    }
	}

	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		return OptionBuilder.withLongOpt(longOpt)
				.withArgName(longOpt.toUpperCase())
				.hasArg(true)
		        .withDescription("filter reads with flags " + longOpt.toUpperCase() + " \n default: " + parameters.getFilterFlags())
		        .create(opt);
	}

}
