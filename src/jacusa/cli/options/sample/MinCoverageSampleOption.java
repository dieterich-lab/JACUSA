package jacusa.cli.options.sample;

import jacusa.cli.options.AbstractACOption;
import jacusa.cli.parameters.SampleParameters;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

public class MinCoverageSampleOption extends AbstractACOption {

	private int sampleI;
	private SampleParameters parameters;
	
	public MinCoverageSampleOption(final int sampleI, final SampleParameters parameters) {
		this.sampleI = sampleI;
		this.parameters = parameters;
		
		opt = "c" + sampleI;
		longOpt = "min-coverage" + sampleI;
	}
	
	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		return OptionBuilder.withLongOpt(longOpt)
					.withArgName(longOpt.toUpperCase())
					.hasArg(true)
			        .withDescription("filter " + sampleI + " positions with coverage < " + longOpt.toUpperCase() + " \n default: " + parameters.getMinCoverage())
			        .create(opt);
	}

	@Override
	public void process(CommandLine line) throws Exception {
	    if(line.hasOption(opt)) {
	    	int minCoverage = Integer.parseInt(line.getOptionValue(opt));
	    	if(minCoverage < 1) {
	    		throw new IllegalArgumentException(longOpt.toUpperCase() + " must be > 0!");
	    	}
	    	parameters.setMinCoverage(minCoverage);
	    }
	}

}
