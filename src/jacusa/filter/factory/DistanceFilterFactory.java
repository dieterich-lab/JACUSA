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

public class DistanceFilterFactory extends AbstractFilterFactory<WindowCache> {

	private static int DISTANCE = 6;
	
	private int distance;
	private AbstractParameters parameters;
	
	private static Set<CigarOperator> cigarOperator = new HashSet<CigarOperator>();
	static {
		cigarOperator.add(CigarOperator.I);
		cigarOperator.add(CigarOperator.D);
		cigarOperator.add(CigarOperator.N);
	}
	
	public DistanceFilterFactory(AbstractParameters parameters) {
		super('D', "Filter distance to Intron and INDEL position. Default: " + DISTANCE + " (D:distance)", cigarOperator);
		this.parameters = parameters;
		distance = DISTANCE;
	}

	@Override
	public void processCLI(String line) throws IllegalArgumentException {
		if (line.length() == 1) {
			return;
		}

		final String[] s = line.split(Character.toString(AbstractFilterFactory.SEP));

		// format D:length
		for (int i = 1; i < s.length; ++i) {
			switch(i) {
			case 1:
				final int distance = Integer.valueOf(s[i]);
				if (distance < 0) {
					throw new IllegalArgumentException("Invalid distance " + line);
				}
				this.distance = distance;
				break;

			default:
				throw new IllegalArgumentException("Invalid argument: " + line);
			}
		}
		
	}

	public DistanceStorageFilter createStorageFilter() {
		return new DistanceStorageFilter(getC(), parameters.getBaseConfig(), parameters.getFilterConfig());
	}

	@Override
	public DistanceFilterStorage createFilterStorage(final WindowCoordinates windowCoordinates, final SampleParameters sampleParameters) {
		return new DistanceFilterStorage(getC(), distance, windowCoordinates, sampleParameters, parameters);
	}
}