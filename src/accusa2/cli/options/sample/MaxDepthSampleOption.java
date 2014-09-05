package accusa2.cli.options.sample;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import accusa2.cli.options.AbstractACOption;
import accusa2.cli.parameters.SampleParameters;

public class MaxDepthSampleOption extends AbstractACOption {

	private char sample;
	private SampleParameters parameters;
	
	public MaxDepthSampleOption(final char sample, final SampleParameters parameters) {
		this.sample = sample;
		this.parameters = parameters;
		longOpt = "max-depth" + sample;
	}
	
	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		return OptionBuilder.withLongOpt(longOpt)
			.withArgName(longOpt.toUpperCase())
			.hasArg()
			.withDescription("max per-sample " + sample + " depth\ndefault: " + parameters.getMaxDepth())
			.create(longOpt);
	}

	@Override
	public void process(CommandLine line) throws Exception {
		if(line.hasOption(longOpt)) {
	    	int maxDepth = Integer.parseInt(line.getOptionValue(opt));
	    	if(maxDepth < 2 || maxDepth == 0) {
	    		throw new IllegalArgumentException(longOpt.toUpperCase() + " must be > 0 or -1 (limited by memory)!");
	    	}
	    	parameters.setMaxDepth(maxDepth);
	    }
	}

}