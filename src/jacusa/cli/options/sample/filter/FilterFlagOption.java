package jacusa.cli.options.sample.filter;

import jacusa.cli.options.AbstractACOption;
import jacusa.cli.parameters.SampleParameters;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

public class FilterFlagOption extends AbstractACOption {

	private SampleParameters[] samples;
	
	public FilterFlagOption(final SampleParameters[] samples) {
		this.samples = samples;

		opt = "F";
		longOpt = "filter-flags";
	}

	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		return OptionBuilder.withLongOpt(longOpt)
				.withArgName(longOpt.toUpperCase())
				.hasArg(true)
		        .withDescription("filter reads with flags " + longOpt.toUpperCase() + " \n default: " + samples[0].getFilterFlags())
		        .create(opt);
	}

	@Override
	public void process(CommandLine line) throws Exception {
		if (line.hasOption(opt)) {
	    	String value = line.getOptionValue(opt);
	    	int filterFlags = Integer.parseInt(value);
	    	if (filterFlags <= 0) {
	    		throw new IllegalArgumentException(longOpt.toUpperCase() + " = " + filterFlags + " not valid.");
	    	}
	    	for (SampleParameters sample : samples) {
	    		sample.setFilterFlags(filterFlags);
	    	}
	    }
	}

}