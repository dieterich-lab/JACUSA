package jacusa.cli.options.sample;

import jacusa.cli.options.AbstractACOption;
import jacusa.cli.parameters.SampleParameters;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

public class MinBASQSampleOption extends AbstractACOption {

	private int sampleI;
	private SampleParameters parameters;
	
	public MinBASQSampleOption(final int sampleI, final SampleParameters parameters) {
		this.sampleI = sampleI;
		this.parameters = parameters;
		
		opt = "q" + sampleI;
		longOpt = "min-basq" + sampleI;
	}
	
	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		return OptionBuilder.withLongOpt(longOpt)
			.withArgName(longOpt.toUpperCase())
			.hasArg(true)
	        .withDescription("filter " + sampleI + " positions with base quality < " + longOpt.toUpperCase() + " \n default: " + parameters.getMinBASQ())
	        .create(opt);
	}

	@Override
	public void process(CommandLine line) throws Exception {
		if(line.hasOption(opt)) {
	    	String value = line.getOptionValue(opt);
	    	byte minBASQ = Byte.parseByte(value);
	    	if(minBASQ < 0) {
	    		throw new IllegalArgumentException(longOpt.toUpperCase() + " = " + minBASQ + " not valid.");
	    	}
	    	parameters.setMinBASQ(minBASQ);
	    }
	}

}