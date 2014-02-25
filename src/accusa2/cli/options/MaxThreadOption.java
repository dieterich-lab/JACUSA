package accusa2.cli.options;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import accusa2.cli.Parameters;

public class MaxThreadOption extends AbstractACOption {

	public MaxThreadOption(Parameters parametrs) {
		super(parametrs);
		opt = 'p';
		longOpt = "threads";
	}
	
	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		return OptionBuilder.withLongOpt(longOpt)
				.withArgName(longOpt.toUpperCase())
				.hasArg(true)
		        .withDescription("use # " + longOpt.toUpperCase() + " \n default: " + parameters.getMaxThreads())
		        .create(opt);
	}

	@Override
	public void process(CommandLine line) throws Exception {
		if(line.hasOption(opt)) {
	    	int maxThreads = Integer.parseInt(line.getOptionValue(opt));
	    	if(maxThreads < 1) {
	    		throw new IllegalArgumentException(longOpt.toUpperCase() + " must be > 0!");
	    	}
	    	parameters.setMaxThreads(maxThreads);
	    }
	}

}
