package jacusa.cli.options.filter;

import jacusa.cli.parameters.SampleParameters;
import jacusa.filter.samtag.SamTagFilter;
import jacusa.filter.samtag.SamTagNHFilter;

public class FilterNHsamTagOption extends AbstractFilterSamTagOption {

	public FilterNHsamTagOption(final int sample, final SampleParameters parameters) {
		super(sample, parameters, "NH");
	}

	@Override
	protected SamTagFilter createSamTagFilter(int value) {
		return new SamTagNHFilter(value);
	}

}