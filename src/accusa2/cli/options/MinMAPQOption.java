package accusa2.cli.options;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import accusa2.cli.Parameters;

public class MinMAPQOption extends AbstractACOption {

	public MinMAPQOption(Parameters parameters) {
		super(parameters);
		opt = 'm';
		longOpt = "min-mapq";
	}
	
	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		return OptionBuilder.withLongOpt(longOpt)
			.withArgName(longOpt.toUpperCase())
			.hasArg(true)
	        .withDescription("filter positions with MAPQ < " + longOpt.toUpperCase() + "\n default: " + parameters.getMinMAPQ())
	        .create(opt);
	}

	@Override
	public void process(CommandLine line) throws Exception {
		if(line.hasOption(opt)) {
	    	String value = line.getOptionValue(opt);
	    	int minMapq = Integer.parseInt(value);
	    	if(minMapq < 0) {
	    		throw new IllegalArgumentException(longOpt.toUpperCase() + " = " + minMapq + " not valid.");
	    	}
	    	parameters.setMinMAPQ(minMapq);
	    }
	}

}
