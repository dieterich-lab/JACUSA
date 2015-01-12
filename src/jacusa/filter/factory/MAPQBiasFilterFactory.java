package jacusa.filter.factory;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.filter.BiasStorageFilter;
import jacusa.filter.storage.bias.BiasContainer;
import jacusa.filter.storage.bias.MAPQBiasFilterStorage;

public class MAPQBiasFilterFactory extends AbstractFilterFactory<BiasContainer> {

	private int maxMAPQ = 60;
	private AbstractParameters parameters;
	
	public MAPQBiasFilterFactory(AbstractParameters parameters) {
		super('M', "");
		desc = "MAPQ bias filter. Default: " + maxMAPQ;
		this.parameters = parameters;
	}

	@Override
	public BiasStorageFilter createStorageFilter() {
		return new BiasStorageFilter(c, maxMAPQ);
	}

	@Override
	public MAPQBiasFilterStorage createFilterStorage() {
		return new MAPQBiasFilterStorage(getC(), maxMAPQ, parameters);
	}

}