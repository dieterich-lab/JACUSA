package jacusa.cli.options.filter;

import jacusa.cli.options.AbstractACOption;
import jacusa.cli.parameters.SampleParameters;
import jacusa.filter.samtag.SamTagFilter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

public abstract class AbstractFilterSamTagOption extends AbstractACOption {

	// private int sample;
	private SampleParameters parameters;
	private String tag;

	public AbstractFilterSamTagOption(int sample, SampleParameters parameters, String tag) {
		// this.sample = sample;
		this.parameters = parameters;
		this.tag = tag;
		longOpt = "filter" + tag + "_" + sample;
	}

	@Override
	public void process(CommandLine line) throws Exception {
		if (line.hasOption(longOpt)) {
	    	int value = Integer.parseInt(line.getOptionValue(longOpt));
	    	parameters.getSamTagFilters().add(createSamTagFilter(value));
	    }
	}

	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		return OptionBuilder.withLongOpt(longOpt)
				.withArgName(tag + "-VALUE")
				.hasArg(true)
		        .withDescription("Max " + tag + "-VALUE for SAM tag " + tag)
		        .create();
	}

	protected abstract SamTagFilter createSamTagFilter(int value);  

}