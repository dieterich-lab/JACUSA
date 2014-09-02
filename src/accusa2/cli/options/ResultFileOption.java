package accusa2.cli.options;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import accusa2.cli.parameters.AbstractParameters;
import accusa2.io.Output;
import accusa2.io.OutputWriter;

public class ResultFileOption extends AbstractACOption {

	private AbstractParameters parameters;
	
	public ResultFileOption(AbstractParameters parameters) {
		this.parameters = parameters;

		opt = 'r';
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
		if(line.hasOption(opt)) {
	    	String resultPathname = line.getOptionValue(opt);
	    	Output output = new OutputWriter(resultPathname);
	    	parameters.setOutput(output);
	    }
	}

}