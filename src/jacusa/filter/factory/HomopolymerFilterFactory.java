package jacusa.filter.factory;

import java.util.HashSet;
import java.util.Set;

import net.sf.samtools.CigarOperator;
import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.filter.AbstractStorageFilter;
import jacusa.filter.HomopolymerStorageFilter;
import jacusa.filter.storage.HomopolymerFilterStorage;
import jacusa.pileup.builder.WindowCache;
import jacusa.util.WindowCoordinates;

public class HomopolymerFilterFactory extends AbstractFilterFactory<WindowCache> {

	private static int LENGTH = 6;
	private int length;
	private AbstractParameters parameters;
	
	private static Set<CigarOperator> cigarOperator = new HashSet<CigarOperator>();
	static {
		cigarOperator.add(CigarOperator.M);
	}
	
	public HomopolymerFilterFactory(final AbstractParameters parameters) {
		super('Y', "Filter wrong variant calls in the vicinity of homopolymers. Default: " + LENGTH + " (Y:length)", cigarOperator);
		this.parameters = parameters;
		length = LENGTH;
	}
	
	@Override
	public void processCLI(final String line) throws IllegalArgumentException {
		if(line.length() == 1) {
			throw new IllegalArgumentException("Invalid argument " + line);
		}

		String[] s = line.split(Character.toString(AbstractFilterFactory.SEP));
		// format Y:length
		for (int i = 1; i < s.length; ++i) {
			int value = Integer.valueOf(s[i]);

			switch(i) {
			case 1:
				setLength(value);
				break;

			default:
				throw new IllegalArgumentException("Invalid argument " + length);
			}
		}
	}

	@Override
	public HomopolymerFilterStorage createFilterStorage(final WindowCoordinates windowCoordinates, final SampleParameters sampleParameters) {
		return new HomopolymerFilterStorage(getC(), length, windowCoordinates, sampleParameters, parameters);
	}

	@Override
	public AbstractStorageFilter<WindowCache> createStorageFilter() {
		return new HomopolymerStorageFilter(getC(), parameters.getBaseConfig(), parameters.getFilterConfig());
	}

	public final void setLength(int length) {
		this.length = length;
	}

	public final int getLength() {
		return length;
	}

}