package accusa2.cli.options.filter;

import accusa2.cli.parameters.SampleParameters;
import accusa2.filter.samtag.SamTagFilter;
import accusa2.filter.samtag.SamTagNHFilter;

public class FilterNHsamTagOption extends AbstractFilterSamTagOption {
	
	public FilterNHsamTagOption(SampleParameters parameters) {
		super(parameters, "NH");
	}

	@Override
	protected SamTagFilter createSamTagFilter(int value) {
		return new SamTagNHFilter(value);
	}

}