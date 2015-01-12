package jacusa.cli.options.sample;

import jacusa.cli.options.AbstractACOption;
import jacusa.cli.parameters.SampleParameters;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

public class MinMAPQSampleOption extends AbstractACOption {

	private int sampleI;
	private SampleParameters parameters;
	
	public MinMAPQSampleOption(final int sampleI, final SampleParameters parameters) {
		this.sampleI = sampleI;
		this.parameters = parameters;

		opt = "m" + sampleI;
		longOpt = "min-mapq" + sampleI;
	}
	
	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		return OptionBuilder.withLongOpt(longOpt)
				.withArgName(longOpt.toUpperCase())
				.hasArg(true)
		        .withDescription("filter " + sampleI + " positions with MAPQ < " + longOpt.toUpperCase() + "\n default: " + parameters.getMinMAPQ())
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
