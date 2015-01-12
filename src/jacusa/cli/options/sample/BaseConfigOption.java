package jacusa.cli.options.sample;

import jacusa.cli.options.AbstractACOption;
import jacusa.cli.parameters.SampleParameters;
import jacusa.pileup.BaseConfig;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

public class BaseConfigOption extends AbstractACOption {

	private int sample;
	private SampleParameters sampleParameters;

	public BaseConfigOption(final int sample, final SampleParameters sampleParameters) {
		this.sample = sample;
		this.sampleParameters = sampleParameters;

		opt = "C" + sample;
		longOpt = "base-config-" + sample;
	}

	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		StringBuilder sb = new StringBuilder();
		for(char c : sampleParameters.getBaseConfig().getBases()) {
			sb.append(c);
		}

		return OptionBuilder.withLongOpt(longOpt)
			.withArgName(longOpt.toUpperCase())
			.hasArg(true)
	        .withDescription("Choose what bases should be considered for variant calling: TC or AG or ACGT or AT...\n default: " + sb.toString())
	        .create(opt);
	}

	@Override
	public void process(CommandLine line) throws Exception {
		if (line.hasOption(opt)) {
	    	char[] values = line.getOptionValue(opt).toCharArray();
	    	if(values.length < 2 || values.length > BaseConfig.VALID.length) {
	    		throw new IllegalArgumentException("Possible values for " + longOpt.toUpperCase() + ": TC, AG, ACGT, AT...");
	    	}
	    	sampleParameters.getBaseConfig().setBases(values);
	    }
	}

	public int getSample() {
		return sample;
	}
	
}