package accusa2.cli.options.filter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import accusa2.cli.options.AbstractACOption;
import accusa2.cli.parameters.SampleParameters;

public class FilterFlagOption extends AbstractACOption {

	private SampleParameters parameters;
	
	public FilterFlagOption(SampleParameters parameters) {
		this.parameters = parameters;

		opt = "F";
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
