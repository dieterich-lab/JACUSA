package accusa2.filter.factory;

import accusa2.filter.process.AbstractPileupBuilderFilter;
import accusa2.filter.process.PolymorphismParallelPileupFilter;

public class PolymorphismPileupFilterFactory extends AbstractFilterFactory {

	public PolymorphismPileupFilterFactory() {
		super('P', "Filter polymorphic positions");
	}

	@Override
	public PolymorphismParallelPileupFilter getParallelPileupFilterInstance() {
		return new PolymorphismParallelPileupFilter(getC());
	}

	@Override
	public AbstractPileupBuilderFilter getPileupBuilderFilterInstance() {
		return null;
	}
	
}
