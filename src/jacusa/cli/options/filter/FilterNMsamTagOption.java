package jacusa.cli.options.filter;

import jacusa.cli.parameters.SampleParameters;
import jacusa.filter.samtag.SamTagFilter;
import jacusa.filter.samtag.SamTagNMFilter;

public class FilterNMsamTagOption extends AbstractFilterSamTagOption {
	
	public FilterNMsamTagOption(final int sample, final SampleParameters paramters) {
		super(sample, paramters, "NM");
	}

	@Override
	protected SamTagFilter createSamTagFilter(int value) {
		return new SamTagNMFilter(value);
	}

}