package jacusa.pileup.iterator;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.filter.FilterContainer;
import jacusa.pileup.builder.AbstractPileupBuilder;
import jacusa.pileup.iterator.location.AbstractLocationAdvancer;
import jacusa.pileup.iterator.variant.Variant;
import jacusa.util.Coordinate;
import jacusa.util.Location;
import net.sf.samtools.SAMFileReader;

public abstract class AbstractOneSampleIterator extends AbstractWindowIterator {

	protected SampleParameters sample;

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
		
		final Location loc = initLocation(
				coordinate, 
				sample.getPileupBuilderFactory().isStranded(), 
				pileupBuilders);
		
		// create the correct LocationAdvancer
		locationAdvancer = AbstractLocationAdvancer.getInstance(
				sample.getPileupBuilderFactory().isStranded(), loc);
	}

	protected boolean hasNext1() {
		return hasNext(locationAdvancer.getLocation(), pileupBuilders);
	}

	public FilterContainer[] getFilterContainers4Replicates1(Location location) {
		return getFilterCaches4Replicates(location, pileupBuilders);
	}

	// ugly code getFilterContainers4Replicates2(Location location)
	public FilterContainer[] getFilterContainers4Replicates2(Location location) {
		return getFilterCaches4Replicates(location, pileupBuilders); 
	}
	
}