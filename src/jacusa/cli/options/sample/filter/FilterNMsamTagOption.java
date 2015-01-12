package jacusa.cli.options.sample.filter;

import jacusa.cli.options.sample.filter.samtag.SamTagFilter;
import jacusa.cli.options.sample.filter.samtag.SamTagNMFilter;
import jacusa.cli.parameters.SampleParameters;

public class FilterNMsamTagOption extends AbstractFilterSamTagOption {
	
	public FilterNMsamTagOption(final int sample, final SampleParameters paramters) {
		super(sample, paramters, "NM");
	}

	@Override
	protected SamTagFilter createSamTagFilter(int value) {
		return new SamTagNMFilter(value);
	}

}