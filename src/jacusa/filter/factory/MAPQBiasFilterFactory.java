package jacusa.filter.factory;

import java.util.HashSet;
import java.util.Set;

import net.sf.samtools.CigarOperator;
import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.filter.BiasBaseCountFilter;
import jacusa.filter.storage.bias.BaseCount;
import jacusa.filter.storage.bias.MAPQBiasFilterStorage;
import jacusa.util.WindowCoordinates;

public class MAPQBiasFilterFactory extends AbstractFilterFactory<BaseCount> {

	private static int MAX_MAPQ = 60;
	private AbstractParameters parameters;

	private static Set<CigarOperator> cigarOperator = new HashSet<CigarOperator>();
	static {
		cigarOperator.add(CigarOperator.M);
	}
	
	public MAPQBiasFilterFactory(AbstractParameters parameters) {
		super('M', "MAPQ bias filter. Max MAPQ Default: " + MAX_MAPQ, cigarOperator);
		this.parameters = parameters;
	}

	@Override
	public BiasBaseCountFilter createStorageFilter() {
		return new BiasBaseCountFilter(getC(), MAX_MAPQ);
	}

	@Override
	public MAPQBiasFilterStorage createFilterStorage(final WindowCoordinates windowCoordinates, final SampleParameters sampleParameters) {
		return new MAPQBiasFilterStorage(getC(), MAX_MAPQ, windowCoordinates, parameters);
	}

}