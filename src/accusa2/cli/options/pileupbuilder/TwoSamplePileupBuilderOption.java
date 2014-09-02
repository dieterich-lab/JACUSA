package accusa2.cli.options.pileupbuilder;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import accusa2.cli.parameters.SampleParameters;

public class TwoSamplePileupBuilderOption extends AbstractPileupBuilderOption {

	private SampleParameters parametersA;
	private SampleParameters parametersB;
	
	public TwoSamplePileupBuilderOption(SampleParameters parametersA, SampleParameters parametersB) {
		this.parametersA = parametersA;
		this.parametersB = parametersB;
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
		if(line.hasOption(opt)) {
	    	char[] value = line.getOptionValue(opt).toCharArray();
	    	if(value.length != 3) {
	    		throw new IllegalArgumentException("Possible values for " + longOpt.toUpperCase() + ": S,S or U,U or S,U or U,S");
	    	}
	    	parametersA.setPileupBuilderFactory(buildPileupBuilderFactory(parse(value[0])));
	    	parametersB.setPileupBuilderFactory(buildPileupBuilderFactory(parse(value[2])));
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