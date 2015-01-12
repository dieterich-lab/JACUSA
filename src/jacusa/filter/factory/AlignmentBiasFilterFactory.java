package jacusa.filter.factory;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.filter.BiasStorageFilter;
import jacusa.filter.storage.bias.AlignmentBiasFilterStorage;
import jacusa.filter.storage.bias.BiasContainer;

public class AlignmentBiasFilterFactory extends AbstractFilterFactory<BiasContainer> {

	private int targetDistance = 100;
	private AbstractParameters parameters;
	
	public AlignmentBiasFilterFactory(AbstractParameters parameters) {
		super('A', "");
		desc = "Alignment bias filter (Alignment). Default: " + targetDistance;
		this.parameters = parameters;
	}

	@Override
	public void processCLI(String line) throws IllegalArgumentException {}

	@Override
	public BiasStorageFilter createStorageFilter() {
		return new BiasStorageFilter(c, targetDistance);
	}

	@Override
	public AlignmentBiasFilterStorage createFilterStorage() {
		return new AlignmentBiasFilterStorage(getC(), targetDistance, parameters);
	}

}