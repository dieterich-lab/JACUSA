package accusa2.filter.factory;

import accusa2.filter.process.AbstractPileupBuilderFilter;
import accusa2.filter.process.DistanceParallelPileupFilter;
import accusa2.filter.process.DistancePileupBuilderFilter;

public class DistanceFilterFactory extends AbstractFilterFactory {

	private static int FILTER_DISTANCE = 6;
	private int filterDistance;
	
	public DistanceFilterFactory() {
		super('D', "Filter distance to start/end of read, intron and INDEL position. Default: " + FILTER_DISTANCE);
		this.filterDistance = FILTER_DISTANCE;
	}
	
	public final int getDistance() {
		return filterDistance;
	}

	public final void setDistance(final int distance) {
		this.filterDistance = distance;
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
		setDistance(distance);
	}

	@Override
	public DistanceParallelPileupFilter getParallelPileupFilterInstance() {
		return new DistanceParallelPileupFilter(getC(), getDistance(), getParameters());
	}

	@Override
	public AbstractPileupBuilderFilter getPileupBuilderFilterInstance() {
		return new DistancePileupBuilderFilter(getC(), getDistance());
	}
	
}
