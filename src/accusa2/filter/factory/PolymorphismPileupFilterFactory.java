package accusa2.filter.factory;

import accusa2.filter.cache.AbstractCountFilterCache;
import accusa2.filter.feature.PolymorphismFilter;

public class PolymorphismPileupFilterFactory extends AbstractFilterFactory {

	public PolymorphismPileupFilterFactory() {
		super('P', "Filter polymorphic positions.");
	}

	@Override
	public PolymorphismFilter getFilterInstance() {
		return new PolymorphismFilter(getC());
	}

	@Override
	public AbstractCountFilterCache getFilterCountInstance() {
		return null;
	}
	
}