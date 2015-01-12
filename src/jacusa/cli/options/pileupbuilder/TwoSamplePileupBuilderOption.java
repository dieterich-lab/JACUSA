package jacusa.cli.options.pileupbuilder;

import jacusa.cli.parameters.SampleParameters;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

public class TwoSamplePileupBuilderOption extends AbstractPileupBuilderOption {

	private SampleParameters parameters1;
	private SampleParameters parameters2;
	
	public TwoSamplePileupBuilderOption(SampleParameters parametersA, SampleParameters parametersB) {
		this.parameters1 = parametersA;
		this.parameters2 = parametersB;
	}

	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		return OptionBuilder.withLongOpt(longOpt)
			.withArgName(longOpt.toUpperCase())
			.hasArg(true)
			.withDescription("Choose how parallel pileups are build: strand specific (" + STRAND_SPECIFIC + ") or strand unspecific (" + STRAND_UNSPECIFIC + ")\n default: " + STRAND_UNSPECIFIC + SEP + STRAND_UNSPECIFIC)
			.create(opt);
	}

	@Override
	public void process(CommandLine line) throws Exception {
		if (line.hasOption(opt)) {
	    	char[] value = line.getOptionValue(opt).toCharArray();
	    	if (value.length != 3) {
	    		throw new IllegalArgumentException("Possible values for " + longOpt.toUpperCase() + ": S,S or U,U or S,U or U,S");
	    	}
	    	parameters1.setPileupBuilderFactory(buildPileupBuilderFactory(parse(value[0])));
	    	parameters2.setPileupBuilderFactory(buildPileupBuilderFactory(parse(value[2])));
	    }
	}

	protected boolean parse(char c) {
		switch(c) {
		case STRAND_SPECIFIC:
			return true;

		case STRAND_UNSPECIFIC:
			return false;

		default:
			throw new IllegalArgumentException("Unknown '" + c + "'! Possible values for " + longOpt.toUpperCase() + ": S,S or U,U or S,U or U,S");
		}
	}

}