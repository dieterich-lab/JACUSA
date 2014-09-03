package accusa2.filter.factory;

import accusa2.cli.parameters.AbstractParameters;
import accusa2.filter.RatioBasedFilter;
import accusa2.filter.cache.AbstractFilterCount;
import accusa2.filter.cache.distance.DistanceFilterCount;

public class DistanceFilterFactory extends AbstractFilterFactory {

	private int distance = 6;
	private AbstractParameters parameters;
	
	public DistanceFilterFactory(AbstractParameters parameters) {
		super('D', "");
		desc = "Filter distance to start/end of read, intron and INDEL position. Default: " + distance;
	}

	@Override
	public void processCLI(String line) throws IllegalArgumentException {
		if(line.length() == 1) {
			return;
		}

		final String[] s = line.split(Character.toString(AbstractFilterFactory.SEP));
		
		final int distance = Integer.valueOf(s[1]);
		if(distance < 0) {
			throw new IllegalArgumentException("Invalid distance " + line);
		}
		this.distance = distance;
	}

	@Override
	public RatioBasedFilter getFilterInstance() {
		return new RatioBasedFilter(getC(), 0.5, parameters.getBaseConfig(), parameters.getFilterConfig());
	}

	@Override
	public AbstractFilterCount getFilterCountInstance() {
		return new DistanceFilterCount(getC(), distance, parameters);
	}

}