package jacusa.cli.options;

import jacusa.cli.parameters.AbstractParameters;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

public class ThreadWindowSizeOption extends AbstractACOption {

	private AbstractParameters parameters; 
	
	public ThreadWindowSizeOption(AbstractParameters parameters) {
		this.parameters = parameters;

		opt = "W";
		longOpt = "thread-window-size";
	}

	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		return OptionBuilder.withLongOpt(longOpt)
			.withArgName("THREAD-WINDOW-SIZE")
			.hasArg(true)
	        .withDescription("size of the window used per thread.\n default: " + parameters.getThreadWindowSize())
	        .create(opt);
	}

	@Override
	public void process(CommandLine line) throws Exception {
		if (line.hasOption(opt)) {
	    	String value = line.getOptionValue(opt);
	    	int windowSize = Integer.parseInt(value);
	    	if (windowSize < 1) {
	    		throw new IllegalArgumentException("THREAD-WINDOW-SIZE too small: " + windowSize);
	    	}

	    	parameters.setThreadWindowSize(windowSize);
		}
	}

}
