package jacusa.cli.options.sample.filter;

import jacusa.cli.options.sample.filter.samtag.SamTagFilter;
import jacusa.cli.options.sample.filter.samtag.SamTagNHFilter;
import jacusa.cli.parameters.SampleParameters;

public class FilterNHsamTagOption extends AbstractFilterSamTagOption {

	public FilterNHsamTagOption(final int sample, final SampleParameters parameters) {
		super(sample, parameters, "NH");
	}

	@Override
	protected SamTagFilter createSamTagFilter(int value) {
		return new SamTagNHFilter(value);
	}

}