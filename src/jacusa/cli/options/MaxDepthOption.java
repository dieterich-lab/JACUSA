package jacusa.cli.options;

import jacusa.cli.parameters.SampleParameters;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

public class MaxDepthOption extends AbstractACOption {

	private SampleParameters[] samples;
	
	public MaxDepthOption(SampleParameters[] samples) {
		this.samples = samples;

		opt = "d";
		longOpt = "max-depth";
	}
	
	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		return OptionBuilder.withLongOpt(longOpt)
			.withArgName(longOpt.toUpperCase())
			.hasArg()
			.withDescription("max per-BAM depth\ndefault: " + samples[0].getMaxDepth())
			.create(opt);
	}

	@Override
	public void process(CommandLine line) throws Exception {
		if (line.hasOption(opt)) {
	    	int maxDepth = Integer.parseInt(line.getOptionValue(opt));
	    	if (maxDepth < 2 || maxDepth == 0) {
	    		throw new IllegalArgumentException(longOpt.toUpperCase() + " must be > 0 or -1 (limited by memory)!");
	    	}
	    	for (SampleParameters sample : samples) {
	    		sample.setMaxDepth(maxDepth);
	    	}
	    	
	    }
	}

}