package jacusa.cli.options.sample;

import jacusa.cli.options.AbstractACOption;
import jacusa.cli.parameters.SampleParameters;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

public class RetainFlagOption extends AbstractACOption {

	private int sample;
	private SampleParameters sampleParameters;
	
	public RetainFlagOption(final int sample, final SampleParameters sampleParameters) {
		this.sample = sample;
		this.sampleParameters = sampleParameters;

		opt = "R" + sample;
		longOpt = "retain-flags-" + sample;
	}

	@Override
	public void process(CommandLine line) throws Exception {
		if (line.hasOption(opt)) {
	    	String value = line.getOptionValue(opt);
	    	int retainFlags = Integer.parseInt(value);
	    	if (retainFlags <= 0) {
	    		throw new IllegalArgumentException(longOpt.toUpperCase() + " = " + retainFlags + " not valid.");
	    	}
	    	sampleParameters.setRetainFlags(retainFlags);
	    }
	}

	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		return OptionBuilder.withLongOpt(longOpt)
				.withArgName(longOpt.toUpperCase())
				.hasArg(true)
		        .withDescription("retain reads with SAM flags " + longOpt.toUpperCase() + " \n default: " + sampleParameters.getRetainFlags())
		        .create(opt);
	}

	public int getSample() {
		return sample;
	}

}