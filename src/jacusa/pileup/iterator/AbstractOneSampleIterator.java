package jacusa.pileup.iterator;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.filter.FilterContainer;
import jacusa.pileup.builder.AbstractPileupBuilder;
import jacusa.pileup.iterator.variant.Variant;
import jacusa.util.Coordinate;
import jacusa.util.Location;
import net.sf.samtools.SAMFileReader;

public abstract class AbstractOneSampleIterator extends AbstractWindowIterator {

	protected SampleParameters sample;

	protected Location location;
	protected final AbstractPileupBuilder[] pileupBuilders;	

	public AbstractOneSampleIterator(
			final Coordinate annotatedCoordinate,
			final Variant filter,
			final SAMFileReader[] readers,
			final SampleParameters sample, 
			AbstractParameters parameters) {
		super(annotatedCoordinate, filter, parameters);

		this.sample = sample;
		pileupBuilders = createPileupBuilders(
				sample.getPileupBuilderFactory(), 
				annotatedCoordinate, 
				readers,
				sample,
				parameters);
		location = initLocation(annotatedCoordinate, sample.getPileupBuilderFactory().isDirected(), pileupBuilders);
	}

	protected boolean hasNextA() {
		return hasNext(location, pileupBuilders);
	}

	public FilterContainer[] getFilterContainers4Replicates1(Location location) {
		return getFilterCaches4Replicates(location, pileupBuilders);
	}

	// ugly code getFilterContainers4Replicates2(Location location)
	public FilterContainer[] getFilterContainers4Replicates2(Location location) {
		return null; 
	}
	
}