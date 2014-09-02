package accusa2.cli.options;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import accusa2.cli.parameters.StatisticParameters;

// FIXME
@Deprecated
public class PermutationsOption  extends AbstractACOption {

	private StatisticParameters parameters;
	
	public PermutationsOption(StatisticParameters parameters) {
		this.parameters = parameters;

		opt = 'A';
		longOpt = "permutation";
	}

	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		return OptionBuilder.withLongOpt(longOpt)
			.withArgName(longOpt.toUpperCase())
			.hasArg(true)
			// FIXME .withDescription("Number of " + longOpt.toUpperCase() + " to estimate FDR\n default: " + parameters.getPermutations())
	        .create(opt);
	}

	@Override
	public void process(CommandLine line) throws Exception {
		if(line.hasOption(opt)) {
		    String value = line.getOptionValue(opt);
	    	int permutations = Integer.parseInt(value);
	    	// FIXME //parameters.setPermutations(permutations);
		}
	}

}