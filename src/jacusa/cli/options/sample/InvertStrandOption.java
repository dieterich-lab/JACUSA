package jacusa.cli.options.sample;

import jacusa.cli.options.AbstractACOption;
import jacusa.cli.parameters.SampleParameters;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

public class InvertStrandOption extends AbstractACOption {

	private int sampleI;
	private SampleParameters parameters;
	
	public InvertStrandOption(final int sampleI, final SampleParameters parameters) {
		this.sampleI = sampleI;
		this.parameters = parameters;

		opt = "i" + sampleI;
		longOpt = "invert-strand" + sampleI;
	}
	
	@Override
	public void process(CommandLine line) throws Exception {
		if(line.hasOption(opt)) {
			parameters.setInvertStrand(true);
	    }
	}

	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		return OptionBuilder.withLongOpt(longOpt)
				.withArgName(longOpt.toUpperCase())
				.hasArg(false)
		        .withDescription("Invert strand of " + sampleI + " sample. Default " + Boolean.toString(parameters.isInvertStrand()))
		        .create(opt);
	}

}