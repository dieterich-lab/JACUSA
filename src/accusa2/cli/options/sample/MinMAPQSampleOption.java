package accusa2.cli.options.sample;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import accusa2.cli.options.AbstractACOption;
import accusa2.cli.parameters.SampleParameters;

public class MinMAPQSampleOption extends AbstractACOption {

	private char sample;
	private SampleParameters parameters;
	
	public MinMAPQSampleOption(final char sample, final SampleParameters parameters) {
		this.sample = sample;
		this.parameters = parameters;

		opt = "m" + sample;
		longOpt = "min-mapq" + sample;
	}
	
	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		return OptionBuilder.withLongOpt(longOpt)
				.withArgName(longOpt.toUpperCase())
				.hasArg(true)
		        .withDescription("filter " + sample + " positions with MAPQ < " + longOpt.toUpperCase() + "\n default: " + parameters.getMinMAPQ())
		        .create(opt);
	}

	@Override
	public void process(CommandLine line) throws Exception {
		if (line.hasOption(opt)) {
	    	String value = line.getOptionValue(opt);
	    	int minMapq = Integer.parseInt(value);
	    	if(minMapq < 0) {
	    		throw new IllegalArgumentException(longOpt.toUpperCase() + " = " + minMapq + " not valid.");
	    	}
	    	parameters.setMinMAPQ(minMapq);
	    }
	}

}
