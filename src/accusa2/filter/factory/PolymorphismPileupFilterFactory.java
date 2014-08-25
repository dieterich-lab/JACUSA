package accusa2.filter.factory;

import accusa2.filter.cache.AbstractPileupBuilderFilterCache;
import accusa2.filter.cache.PolymorphismParallelPileupFilter;

public class PolymorphismPileupFilterFactory extends AbstractFilterFactory {

	public PolymorphismPileupFilterFactory() {
		super('P', "Filter polymorphic positions"); // TODO make better description
	}

	@Override
	public PolymorphismParallelPileupFilter getFilterInstance() {
		return new PolymorphismParallelPileupFilter(getC());
	}

	@Override
	public AbstractPileupBuilderFilterCache getCacheInstance() {
		return null;
	}
	
}