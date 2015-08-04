package jacusa.filter.factory;


import java.util.HashSet;
import java.util.Set;

import net.sf.samtools.CigarOperator;
import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.filter.BiasBaseCountFilter;
import jacusa.filter.storage.bias.AlignmentBiasFilterStorage;
import jacusa.filter.storage.bias.BaseCount;
import jacusa.util.WindowCoordinates;

@Deprecated
public class AlignmentBiasFilterFactory extends AbstractFilterFactory<BaseCount> {

	private static int TARGET_DISTANCE = 100;
	private AbstractParameters parameters;

	private static Set<CigarOperator> cigarOperator = new HashSet<CigarOperator>();
	static {
		cigarOperator.add(CigarOperator.M);
	}
	
	public AlignmentBiasFilterFactory(final AbstractParameters parameters) {
		super('A', "Alignment bias filter (Alignment). Default: " + TARGET_DISTANCE, cigarOperator);
		this.parameters = parameters;
	}

	@Override
	public void processCLI(final String line) throws IllegalArgumentException {
		/* 
		if (line.length() == 1) {
			throw new IllegalArgumentException("Invalid argument " + line);
		}

		final String[] s = line.split(Character.toString(AbstractFilterFactory.SEP));
		TARGET_DISTANCE = Integer.parseInt(s[1]);
		*/
	}

	@Override
	public BiasBaseCountFilter createStorageFilter() {
		return new BiasBaseCountFilter(getC(), TARGET_DISTANCE);
	}

	@Override
	public AlignmentBiasFilterStorage createFilterStorage(final WindowCoordinates windowCoordinates, final SampleParameters sampleParameters) {
		return new AlignmentBiasFilterStorage(getC(), TARGET_DISTANCE, windowCoordinates, parameters);
	}

}