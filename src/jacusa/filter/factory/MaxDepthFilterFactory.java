package jacusa.filter.factory;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.cli.parameters.hasSample2;
import jacusa.filter.AbstractStorageFilter;
import jacusa.filter.MaxDepthStorageFilter;
import jacusa.filter.storage.DummyFilterFillCache;
import jacusa.util.WindowCoordinates;

public class MaxDepthFilterFactory extends AbstractFilterFactory<Void> {

	public static final char C = 'd';
	
	private AbstractParameters parameters;
	
	public MaxDepthFilterFactory(AbstractParameters parameters) {
		super(C, "");
		this.parameters = parameters;
	}

	@Override
	public DummyFilterFillCache createFilterStorage(final WindowCoordinates windowCoordinates, final SampleParameters sampleParameters) {
		// storage is not needed - done 
		// Low Quality Base Calls are stored in AbstractBuilder 
		return new DummyFilterFillCache(getC());
	}

	@Override
	public AbstractStorageFilter<Void> createStorageFilter() {
		if (parameters instanceof hasSample2) {
			return new MaxDepthStorageFilter(getC(), parameters.getSample1(), ((hasSample2)parameters).getSample2());
		}
		return new MaxDepthStorageFilter(getC(), parameters.getSample1(), null);
	}
	
}