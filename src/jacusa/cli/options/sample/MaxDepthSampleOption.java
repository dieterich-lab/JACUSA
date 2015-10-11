package jacusa.cli.options.sample;

import jacusa.cli.options.AbstractACOption;
import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.filter.factory.MaxDepthFilterFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

public class MaxDepthSampleOption extends AbstractACOption {

	private int sampleI;
	private SampleParameters sampleParameters;
	private AbstractParameters parameters;
	
	public MaxDepthSampleOption(
			final int sampleI, 
			final SampleParameters sampleParameters,
			final AbstractParameters parameters) {
		this.sampleI = sampleI;
		this.sampleParameters = sampleParameters;
		this.parameters = parameters;

		opt = "d" + sampleI;
		longOpt = "max-depth" + sampleI;
	}
	
	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		return OptionBuilder.withLongOpt(longOpt)
			.withArgName(longOpt.toUpperCase())
			.hasArg()
			.withDescription("max per-sample " + sampleI + " depth\ndefault: " + sampleParameters.getMaxDepth())
			.create(opt);
	}

	@Override
	public void process(CommandLine line) throws Exception {
		if(line.hasOption(opt)) {
	    	int maxDepth = Integer.parseInt(line.getOptionValue(opt));
	    	if(maxDepth < 2 || maxDepth == 0) {
	    		throw new IllegalArgumentException(longOpt.toUpperCase() + " must be > 0 or -1 (limited by memory)!");
	    	}
	    	sampleParameters.setMaxDepth(maxDepth);

	    	if (! parameters.getFilterConfig().hasFilter(MaxDepthFilterFactory.C)) {
	    		parameters.getFilterConfig().getFactories().add(new MaxDepthFilterFactory(parameters));
	    	}

	    }
	}

}