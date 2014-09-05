package accusa2.cli.options.sample;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import accusa2.cli.options.AbstractACOption;
import accusa2.cli.parameters.SampleParameters;

public class MinCoverageSampleOption extends AbstractACOption {

	private char sample;
	private SampleParameters parameters;
	
	public MinCoverageSampleOption(final char sample, final SampleParameters parameters) {
		this.sample = sample;
		this.parameters = parameters;

		longOpt = "min-coverage" + sample;
	}
	
	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		return OptionBuilder.withLongOpt(longOpt)
					.withArgName(longOpt.toUpperCase())
					.hasArg(true)
			        .withDescription("filter " + sample + " positions with coverage < " + longOpt.toUpperCase() + " \n default: " + parameters.getMinCoverage())
			        .create(longOpt);
	}

	@Override
	public void process(CommandLine line) throws Exception {
	    if(line.hasOption(longOpt)) {
	    	int minCoverage = Integer.parseInt(line.getOptionValue(opt));
	    	if(minCoverage < 1) {
	    		throw new IllegalArgumentException(longOpt.toUpperCase() + " must be > 0!");
	    	}
	    	parameters.setMinCoverage(minCoverage);
	    }
	}

}
