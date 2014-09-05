package accusa2.cli.options;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import accusa2.cli.parameters.SampleParameters;

public class MinMAPQOption extends AbstractACOption {

	private SampleParameters sampleA;
	private SampleParameters sampleB;
	
	public MinMAPQOption(SampleParameters sampleA, SampleParameters sampleB) {
		this.sampleA = sampleA;
		this.sampleA = sampleA;

		opt = 'm';
		longOpt = "min-mapq";
	}


	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		return OptionBuilder.withLongOpt(longOpt)
			.withArgName(longOpt.toUpperCase())
			.hasArg(true)
	        .withDescription("filter positions with MAPQ < " + longOpt.toUpperCase() + "\n default: " + sampleA.getMinMAPQ())
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
	    	sampleA.setMinMAPQ(minMapq);
	    	sampleB.setMinMAPQ(minMapq);
	    }
	}

}