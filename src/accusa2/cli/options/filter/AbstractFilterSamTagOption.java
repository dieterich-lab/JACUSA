package accusa2.cli.options.filter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import accusa2.cli.Parameters;
import accusa2.cli.options.AbstractACOption;
import accusa2.filter.samtag.SamTagFilter;

public abstract class AbstractFilterSamTagOption extends AbstractACOption {

	private String tag;

	public AbstractFilterSamTagOption(Parameters parameters, String tag) {
		super(parameters);
		this.tag = tag;
		longOpt = "filter" + tag;
	}

	@Override
	public void process(CommandLine line) throws Exception {
		if(line.hasOption(longOpt)) {
	    	int value = Integer.parseInt(line.getOptionValue(longOpt));
	    	parameters.getSamTagFilter().add(createSamTagFilter(value));
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
