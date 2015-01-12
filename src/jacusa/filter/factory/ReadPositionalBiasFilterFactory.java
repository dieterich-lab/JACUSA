package jacusa.filter.factory;

import java.util.HashSet;
import java.util.Set;

import net.sf.samtools.CigarOperator;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.filter.BiasBaseCountFilter;
import jacusa.filter.storage.bias.BaseCount;
import jacusa.filter.storage.bias.ReadPositionBiasFilterStorage;
import jacusa.util.WindowCoordinates;

public class ReadPositionalBiasFilterFactory extends AbstractFilterFactory<BaseCount> {

	private int targetReadLength = 50;
	private AbstractParameters parameters;
	
	private static Set<CigarOperator> cigarOperator = new HashSet<CigarOperator>();
	static {
		cigarOperator.add(CigarOperator.M);
	}
	
	public ReadPositionalBiasFilterFactory(final AbstractParameters parameters) {
		super('P', "Positional bias filter (read position)", cigarOperator);
		this.parameters = parameters;
	}

	@Override
	public void processCLI(final String line) throws IllegalArgumentException {
		if(line.length() == 1) {
			throw new IllegalArgumentException("Invalid argument " + line);
		}

		String[] s = line.split(Character.toString(AbstractFilterFactory.SEP));
		// format P:targetReadLength
		targetReadLength = Integer.parseInt(s[1]);
	}

	@Override
	public BiasBaseCountFilter createStorageFilter() {
		return new BiasBaseCountFilter(getC(), targetReadLength);
	}

	@Override
	public ReadPositionBiasFilterStorage createFilterStorage(final WindowCoordinates windowCoordinates, final SampleParameters sampleParameters) {
		return new ReadPositionBiasFilterStorage(getC(), targetReadLength, windowCoordinates, parameters);
	}

}