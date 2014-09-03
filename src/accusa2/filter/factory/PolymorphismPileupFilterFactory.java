package accusa2.filter.factory;

import accusa2.filter.cache.AbstractFilterCount;
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
	public AbstractFilterCount getFilterCountInstance() {
		return null;
	}
	
}