package jacusa.filter.factory;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.filter.AbstractStorageFilter;
import jacusa.filter.BiasStorageFilter;
import jacusa.filter.storage.bias.BiasContainer;
import jacusa.filter.storage.bias.ReadPositionBiasFilterStorage;

public class PositionalBiasFilterFactory extends AbstractFilterFactory<BiasContainer> {

	private int targetReadLength = 50;
	private AbstractParameters parameters;

	public PositionalBiasFilterFactory(AbstractParameters parameters) {
		super('P', "");
		desc = "Positional bias filter (read position). Default: " + targetReadLength;
		this.parameters = parameters;
	}

	@Override
	public void processCLI(String line) throws IllegalArgumentException {}

	@Override
	public AbstractStorageFilter<BiasContainer> createStorageFilter() {
		return new BiasStorageFilter(c, targetReadLength);
	}

	@Override
	public ReadPositionBiasFilterStorage createFilterStorage() {
		return new ReadPositionBiasFilterStorage(getC(), targetReadLength, parameters);
	}

}