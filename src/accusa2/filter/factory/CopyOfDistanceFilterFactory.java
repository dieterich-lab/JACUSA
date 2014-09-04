package accusa2.filter.factory;

import accusa2.filter.cache.AbstractCountFilterCache;
import accusa2.filter.feature.AbstractFeatureFilter;

//TODO make this generic
public class CopyOfDistanceFilterFactory extends AbstractFilterFactory {

	@Override
	public AbstractFeatureFilter getFilterInstance() {
		return null;
	}
	
	// options
	// RS Read_Start
	// RE Reand_End
	// SJ SpliceJunction
	// ID InDel
	// HP HomoPolymer
	// ALL

	public CopyOfDistanceFilterFactory() {
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

	
/*	
	public DistanceFilter getFilterInstance() {
		return new DistanceFilter(getC(), 0, getParameters());
	}
*/
	
	@Override
	public AbstractCountFilterCache getFilterCountInstance() {
		return null; // FIXME
	}

	public enum DISTANCE_FILTER {ALL, RS, RE, SJ, ID, HP}

}