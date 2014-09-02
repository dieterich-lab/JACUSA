package accusa2.filter.factory;

import accusa2.cli.parameters.AbstractParameters;
import accusa2.filter.FilterConfig;
import accusa2.filter.RatioBasedFilter;
import accusa2.filter.cache.AbstractPileupBuilderFilterCount;
import accusa2.filter.cache.distance.DistanceFilterCount;
import accusa2.pileup.BaseConfig;

public class DistanceFilterFactory extends AbstractFilterFactory {

	private int distance = 6;
	
	public DistanceFilterFactory(BaseConfig baseConfig, FilterConfig filterConfig) {
		super('D', "", filterConfig);
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
		return new RatioBasedFilter(getC(), 0.5);
	}

	@Override
	public AbstractPileupBuilderFilterCount getFilterCountInstance() {
		return new DistanceFilterCount(getC(), distance, parameters);
	}

}