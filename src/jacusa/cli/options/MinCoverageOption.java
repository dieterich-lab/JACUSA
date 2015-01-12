package jacusa.cli.options;

import jacusa.cli.parameters.SampleParameters;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

public class MinCoverageOption extends AbstractACOption {

	private SampleParameters[] samples;
	
	public MinCoverageOption(SampleParameters[] samples) {
		this.samples = samples;

		opt = "c";
		longOpt = "min-coverage";
	}

	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		return OptionBuilder.withLongOpt(longOpt)
					.withArgName(longOpt.toUpperCase())
					.hasArg(true)
			        .withDescription("filter positions with coverage < " + longOpt.toUpperCase() + " \n default: " + samples[0].getMinCoverage())
			        .create(opt);
	}

	@Override
	public void process(CommandLine line) throws Exception {
	    if (line.hasOption(opt)) {
	    	int minCoverage = Integer.parseInt(line.getOptionValue(opt));
	    	if (minCoverage < 1) {
	    		throw new IllegalArgumentException(longOpt.toUpperCase() + " must be > 0!");
	    	}
	    	
	    	for (SampleParameters sample : samples) {
	    		sample.setMinCoverage(minCoverage);
	    	}
	    }
	}

}