package jacusa.filter.factory;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.filter.DistanceStorageFilter;
import jacusa.filter.storage.DistanceFilterStorage;
import jacusa.pileup.builder.WindowCache;

public class DistanceFilterFactory extends AbstractFilterFactory<WindowCache> {

	private int distance = 6;
	private AbstractParameters parameters;
	
	public DistanceFilterFactory(AbstractParameters parameters) {
		super('D', "");
		desc = "Filter distance to Intron and INDEL position. Default: " + distance;
		this.parameters = parameters;
	}

	@Override
	public void processCLI(String line) throws IllegalArgumentException {
		if (line.length() == 1) {
			return;
		}

		final String[] s = line.split(Character.toString(AbstractFilterFactory.SEP));

		final int distance = Integer.valueOf(s[1]);
		if (distance < 0) {
			throw new IllegalArgumentException("Invalid distance " + line);
		}
		this.distance = distance;
	}

	public DistanceStorageFilter createStorageFilter() {
		return new DistanceStorageFilter(c, parameters.getBaseConfig(), parameters.getFilterConfig());
	}
	
	@Override
	public DistanceFilterStorage createFilterStorage() {
		return new DistanceFilterStorage(getC(), distance, parameters);
	}
}