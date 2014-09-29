package accusa2.cli.options.pileupbuilder;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import accusa2.cli.parameters.SampleParameters;

public class OneSamplePileupBuilderOption extends AbstractPileupBuilderOption {

	private SampleParameters parameters;
	
	public OneSamplePileupBuilderOption(SampleParameters parameters) {
		super();
		
		this.parameters = parameters;
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
	    	parameters.setPileupBuilderFactory(buildPileupBuilderFactory(parse(value[0])));
	    }
	}

}