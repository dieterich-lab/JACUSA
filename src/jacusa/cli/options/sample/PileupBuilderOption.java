package jacusa.cli.options.sample;

import jacusa.cli.options.AbstractACOption;
import jacusa.cli.parameters.SampleParameters;
import jacusa.pileup.builder.DirectedPileupBuilderFactory;
import jacusa.pileup.builder.PileupBuilderFactory;
import jacusa.pileup.builder.UndirectedPileupBuilderFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

public class PileupBuilderOption extends AbstractACOption {

	protected static final char STRAND_SPECIFIC 	= 'S';
	protected static final char STRAND_UNSPECIFIC 	= 'U';
	private SampleParameters sample;

	public PileupBuilderOption(SampleParameters sample) {
		super();

		this.sample = sample;
	}
	
	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		return OptionBuilder.withLongOpt(longOpt)
			.withArgName(longOpt.toUpperCase())
			.hasArg(true)
	        .withDescription("Choose how parallel pileups are build: strand specific (" + STRAND_SPECIFIC + ") or strand unspecific (" + STRAND_UNSPECIFIC + ")\n default: " + STRAND_UNSPECIFIC)
	        .create(opt);
	}

	@Override
	public void process(CommandLine line) throws Exception {
		if (line.hasOption(opt)) {
	    	char[] value = line.getOptionValue(opt).toCharArray();
	    	if (value.length != 1) {
	    		throw new IllegalArgumentException("Possible values for " + longOpt.toUpperCase() + ": S or U");
	    	}
	    	sample.setPileupBuilderFactory(buildPileupBuilderFactory(parse(value[0])));
	    }
	}

	protected PileupBuilderFactory buildPileupBuilderFactory(boolean isDirected) {
		if (isDirected) {
			return new DirectedPileupBuilderFactory();
		} else {
			return new UndirectedPileupBuilderFactory();
		}
	}
	
	protected boolean parse(char c) {
		switch(c) {

		case STRAND_SPECIFIC:
			return true;

		case STRAND_UNSPECIFIC:
			return false;

		default:
			throw new IllegalArgumentException("Unknown '" + c + "'! Possible values for " + longOpt.toUpperCase() + ": S or U");
		}
	}
	
}