package accusa2.cli.options;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import accusa2.cli.parameters.SampleParameters;

public class MinCoverageOption extends AbstractACOption {

	private SampleParameters parameters;
	
	public MinCoverageOption(SampleParameters parameters) {
		this.parameters = parameters;

		opt = 'c';
		longOpt = "min-coverage";
	}
	
	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		return OptionBuilder.withLongOpt(longOpt)
					.withArgName(longOpt.toUpperCase())
					.hasArg(true)
			        .withDescription("filter positions with coverage < " + longOpt.toUpperCase() + " \n default: " + parameters.getMinCoverage())
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
