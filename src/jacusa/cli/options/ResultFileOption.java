package jacusa.cli.options;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.io.Output;
import jacusa.io.OutputWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

public class ResultFileOption extends AbstractACOption {

	private AbstractParameters parameters;
	
	public ResultFileOption(AbstractParameters parameters) {
		this.parameters = parameters;

		opt = "r";
		longOpt = "result-file";
	}

	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		return OptionBuilder.withLongOpt(longOpt)
			.withArgName(longOpt.toUpperCase())
			.hasArg(true)
	        .withDescription("results are written to " + longOpt.toUpperCase() + " or STDOUT if empty")
	        .create(opt);
	}

	@Override
	public void process(CommandLine line) throws Exception {
		if (line.hasOption(opt)) {
	    	String resultPathname = line.getOptionValue(opt);
	    	Output output = new OutputWriter(resultPathname);
	    	parameters.setOutput(output);
	    }
	}

}