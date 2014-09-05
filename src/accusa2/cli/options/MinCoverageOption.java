package accusa2.cli.options;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import accusa2.cli.parameters.SampleParameters;

public class MinCoverageOption extends AbstractACOption {

	private SampleParameters sampleA;
	private SampleParameters sampleB;
	
	public MinCoverageOption(SampleParameters sampleA, SampleParameters sampleB) {
		this.sampleA = sampleA;
		this.sampleB = sampleB;

		opt = "c";
		longOpt = "min-coverage";
	}
	
	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		return OptionBuilder.withLongOpt(longOpt)
					.withArgName(longOpt.toUpperCase())
					.hasArg(true)
			        .withDescription("filter positions with coverage < " + longOpt.toUpperCase() + " \n default: " + sampleA.getMinCoverage())
			        .create(opt);
	}

	@Override
	public void process(CommandLine line) throws Exception {
	    if(line.hasOption(opt)) {
	    	int minCoverage = Integer.parseInt(line.getOptionValue(opt));
	    	if(minCoverage < 1) {
	    		throw new IllegalArgumentException(longOpt.toUpperCase() + " must be > 0!");
	    	}
	    	sampleA.setMinCoverage(minCoverage);
	    	sampleB.setMinCoverage(minCoverage);
	    }
	}

}
