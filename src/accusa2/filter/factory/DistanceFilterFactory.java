package accusa2.filter.factory;

import accusa2.filter.cache.AbstractPileupBuilderFilterCache;
import accusa2.filter.cache.distance.DistanceParallelPileupFilter;
import accusa2.filter.cache.distance.DistancePileupBuilderCache;

//TODO make this generic
public class DistanceFilterFactory extends AbstractFilterFactory {

	// options
	// RS Read_Start
	// RE Reand_End
	// SJ SpliceJunction
	// ID InDel
	// HP HomoPolymer

	public DistanceFilterFactory() {
		super('D', "Filter distance to start/end of read, intron and INDEL position. Default: ");
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
		
		
	}

	// TODO
	/*
	private AbstractDistanceFilter create(DISTANCE_FILTER op) {
		switch (op) {
		case RS:
		case RE:
		case SJ:
		case ID:
		case HP:
			break;
		}
		
		return null;
	}
	*/

	// TODO
	@Override
	public DistanceParallelPileupFilter getFilterInstance() {
		return new DistanceParallelPileupFilter(getC(), 0, getParameters());
	}

	// TODO
	@Override
	public AbstractPileupBuilderFilterCache getCacheInstance() {
		return new DistancePileupBuilderCache(getC(), 0, getParameters());
	}
	
	public enum DISTANCE_FILTER {RS, RE, SJ, ID, HP}

}