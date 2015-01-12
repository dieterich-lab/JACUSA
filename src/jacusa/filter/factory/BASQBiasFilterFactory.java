package jacusa.filter.factory;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.filter.AbstractStorageFilter;
import jacusa.filter.BiasStorageFilter;
import jacusa.filter.storage.bias.BASQBiasFilterStorage;
import jacusa.filter.storage.bias.BiasContainer;
import jacusa.process.phred2prob.Phred2Prob;

public class BASQBiasFilterFactory extends AbstractFilterFactory<BiasContainer> {

	private int maxBASQ = Phred2Prob.MAX_Q;
	private AbstractParameters parameters;
	
	public BASQBiasFilterFactory(AbstractParameters parameters) {
		super('B', "");
		desc = "BASQ bias filter. Default: " + maxBASQ;
		this.parameters = parameters;
	}

	@Override
	public AbstractStorageFilter<BiasContainer> createStorageFilter() {
		return new BiasStorageFilter(c, maxBASQ);
	}

	@Override
	public BASQBiasFilterStorage createFilterStorage() {
		return new BASQBiasFilterStorage(getC(), maxBASQ, parameters);
	}

}