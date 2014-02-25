package accusa2.cli.options;

import org.apache.commons.cli.CommandLine;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import accusa2.cli.Parameters;

public class PermutationsOption  extends AbstractACOption {

	public PermutationsOption(Parameters parameters) {
		super(parameters);
		opt = 'A';
		longOpt = "permutation";
	}

	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		return OptionBuilder.withLongOpt(longOpt)
			.withArgName(longOpt.toUpperCase())
			.hasArg(true)
	        .withDescription("Number of " + longOpt.toUpperCase() + " to estimate FDR\n default: " + parameters.getPermutations())
	        .create(opt);
	}

	@Override
	public void process(CommandLine line) throws Exception {
		if(line.hasOption(opt)) {
		    String value = line.getOptionValue(opt);
	    	int permutations = Integer.parseInt(value);
	    	parameters.setPermutations(permutations);
		}
	}

}
