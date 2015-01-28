package jacusa.filter.factory;

import java.util.HashSet;
import java.util.Set;

import net.sf.samtools.CigarOperator;
import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.filter.DistanceStorageFilter;
import jacusa.filter.storage.DistanceFilterStorage;
import jacusa.pileup.builder.WindowCache;
import jacusa.util.WindowCoordinates;

public class INDEL_DistanceFilterFactory extends AbstractFilterFactory<WindowCache> {

	private static int DISTANCE = 6;
	private static double MIN_RATIO = 0.5;
	private static int MIN_COUNT = 2;

	private int distance;
	private double minRatio;
	private int minCount;

	private AbstractParameters parameters;
	
	private static Set<CigarOperator> cigarOperator = new HashSet<CigarOperator>();
	static {
		cigarOperator.add(CigarOperator.I);
	}
	
	public INDEL_DistanceFilterFactory(AbstractParameters parameters) {
		super('I', "Filter distance to INDEL position. Default: " + DISTANCE + ":" + MIN_RATIO + ":" + MIN_COUNT +" (I:distance:min_ratio:min_count)", cigarOperator);
		this.parameters = parameters;
		distance = DISTANCE;
		minRatio = MIN_RATIO;
		minCount = MIN_COUNT;
	}

	@Override
	public void processCLI(String line) throws IllegalArgumentException {
		if (line.length() == 1) {
			return;
		}

		final String[] s = line.split(Character.toString(AbstractFilterFactory.SEP));

		// format D:distance:minRatio:minCount
		for (int i = 1; i < s.length; ++i) {
			switch(i) {
			case 1:
				final int distance = Integer.valueOf(s[i]);
				if (distance < 0) {
					throw new IllegalArgumentException("Invalid distance " + line);
				}
				this.distance = distance;
				break;

			case 2:
				final double minRatio = Double.valueOf(s[i]);
				if (minRatio < 0.0 || minRatio > 1.0) {
					throw new IllegalArgumentException("Invalid minRatio " + line);
				}
				this.minRatio = minRatio;
				break;
			
			case 3:
				final int minCount = Integer.valueOf(s[i]);
				if (minCount < 0) {
					throw new IllegalArgumentException("Invalid minCount " + line);
				}
				this.minCount = minCount;
				break;
				
			default:
				throw new IllegalArgumentException("Invalid argument: " + line);
			}
		}
	}

	public DistanceStorageFilter createStorageFilter() {
		return new DistanceStorageFilter(getC(), minRatio, minCount, parameters.getBaseConfig(), parameters.getFilterConfig());
	}

	@Override
	public DistanceFilterStorage createFilterStorage(final WindowCoordinates windowCoordinates, final SampleParameters sampleParameters) {
		return new DistanceFilterStorage(getC(), distance, windowCoordinates, sampleParameters, parameters);
	}
}