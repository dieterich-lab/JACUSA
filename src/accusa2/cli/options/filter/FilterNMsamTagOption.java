package accusa2.cli.options.filter;

import accusa2.cli.Parameters;
import accusa2.filter.samtag.SamTagFilter;
import accusa2.filter.samtag.SamTagNMFilter;

public class FilterNMsamTagOption extends AbstractFilterSamTagOption {

	public FilterNMsamTagOption(Parameters paramters) {
		super(paramters, "NM");
	}

	@Override
	protected SamTagFilter createSamTagFilter(int value) {
		return new SamTagNMFilter(value);
	}

}
