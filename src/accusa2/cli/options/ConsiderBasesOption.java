package accusa2.cli.options;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import accusa2.cli.Parameters;
import accusa2.pileup.BaseConfig;

public class ConsiderBasesOption extends AbstractACOption {

	public ConsiderBasesOption(Parameters parameters) {
		super(parameters);
		opt = 'B';
		longOpt = "bases";
	}
	
	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		StringBuilder sb = new StringBuilder();
		for(char c : parameters.getBaseConfig().getBases()) {
			sb.append(c);
		}

		return OptionBuilder.withLongOpt(longOpt)
			.withArgName(longOpt.toUpperCase())
			.hasArg(true)
	        .withDescription("Choose what bases should be considered for variant calling: TC or AG or ACGT or AT...\n default: " + sb.toString())
	        .create(opt);
	}

	@Override
	public void process(CommandLine line) throws Exception {
		if(line.hasOption(opt)) {
	    	char[] values = line.getOptionValue(opt).toCharArray();
	    	if(values.length < 2 || values.length > BaseConfig.VALID.length) {
	    		throw new IllegalArgumentException("Possible values for " + longOpt.toUpperCase() + ": TC, AG, ACGT, AT...");
	    	}
	    	parameters.getBaseConfig().setBases(values);
	    }
	}

}