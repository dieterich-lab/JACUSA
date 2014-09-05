package accusa2.cli.options.sample;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import accusa2.cli.options.AbstractACOption;
import accusa2.cli.parameters.SampleParameters;

public class MinBASQSampleOption extends AbstractACOption {

	private char sample;
	private SampleParameters parameters;
	
	public MinBASQSampleOption(final char sample, final SampleParameters parameters) {
		this.sample = sample;
		this.parameters = parameters;
		longOpt = "min-basq" + sample;
	}
	
	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		return OptionBuilder.withLongOpt(longOpt)
			.withArgName(longOpt.toUpperCase())
			.hasArg(true)
	        .withDescription("filter " + sample + " positions with base quality < " + longOpt.toUpperCase() + " \n default: " + parameters.getMinBASQ())
	        .create(longOpt);
	}

	@Override
	public void process(CommandLine line) throws Exception {
		if(line.hasOption(longOpt)) {
	    	String value = line.getOptionValue(opt);
	    	byte minBASQ = Byte.parseByte(value);
	    	if(minBASQ < 0) {
	    		throw new IllegalArgumentException(longOpt.toUpperCase() + " = " + minBASQ + " not valid.");
	    	}
	    	parameters.setMinBASQ(minBASQ);
	    }
	}

}