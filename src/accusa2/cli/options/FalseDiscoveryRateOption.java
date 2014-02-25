package accusa2.cli.options;

import org.apache.commons.cli.CommandLine;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import accusa2.cli.Parameters;

public class FalseDiscoveryRateOption  extends AbstractACOption {

	public FalseDiscoveryRateOption(Parameters parameters) {
		super(parameters);
		opt = 'T';
		longOpt = "fdr";
	}

	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		return OptionBuilder.withLongOpt(longOpt)
			.withArgName(longOpt.toUpperCase())
			.hasArg(true)
	        .withDescription("Filter positions dependening on the " + longOpt.toUpperCase() + "\n default: " + parameters.getFDR())
	        .create(opt);
	}

	@Override
	public void process(CommandLine line) throws Exception {
		if(line.hasOption(opt)) {
		    String value = line.getOptionValue(opt);
	    	double fdr = Double.parseDouble(value);
	    	if(fdr < 0 || fdr > 1) {
	    		throw new Exception("Invalid value for " + longOpt.toUpperCase() + ". Allowed values are 0 <= " + longOpt.toUpperCase() + " <= 1");
	    	}
	    	parameters.setFDR(fdr);
		}
	}

}
