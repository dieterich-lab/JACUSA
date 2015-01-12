package jacusa.filter.factory;

import java.util.HashSet;
import java.util.Set;

import net.sf.samtools.CigarOperator;
import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.filter.AbstractStorageFilter;
import jacusa.filter.BiasBaseCountFilter;
import jacusa.filter.storage.AbstractFilterStorage;
import jacusa.filter.storage.bias.BASQBiasFilterStorage;
import jacusa.filter.storage.bias.BaseCount;
import jacusa.phred2prob.Phred2Prob;
import jacusa.util.WindowCoordinates;

public class BASQBiasFilterFactory extends AbstractFilterFactory<BaseCount> {

	private static int maxBASQ = Phred2Prob.MAX_Q;
	private AbstractParameters parameters;

	private static Set<CigarOperator> cigarOperator = new HashSet<CigarOperator>();
	static {
		cigarOperator.add(CigarOperator.M);
	}
	
	public BASQBiasFilterFactory(AbstractParameters parameters) {
		super('Q', "BASQ bias filter. Default: " + maxBASQ, cigarOperator);
		this.parameters = parameters;
	}

	@Override
	public AbstractStorageFilter<BaseCount> createStorageFilter() {
		return new BiasBaseCountFilter(getC(), maxBASQ);
	}

	public AbstractFilterStorage<BaseCount> createFilterStorage(final WindowCoordinates windowCoordinates, final SampleParameters sampleParameters) {
		return new BASQBiasFilterStorage(getC(), maxBASQ, windowCoordinates, parameters);
	}
}