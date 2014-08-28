package accusa2.filter.factory;

import accusa2.filter.PolymorphismFilter;
import accusa2.filter.cache.AbstractPileupBuilderFilterCount;

public class PolymorphismPileupFilterFactory extends AbstractFilterFactory {

	public PolymorphismPileupFilterFactory() {
		super('P', "Filter polymorphic positions.");
	}

	@Override
	public PolymorphismFilter getFilterInstance() {
		return new PolymorphismFilter(getC());
	}

	@Override
	public AbstractPileupBuilderFilterCount getFilterCountInstance() {
		return null;
	}
	
}