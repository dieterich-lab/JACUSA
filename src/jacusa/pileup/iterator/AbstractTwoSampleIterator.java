package jacusa.pileup.iterator;


import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.filter.FilterContainer;
import jacusa.pileup.DefaultParallelPileup;
import jacusa.pileup.builder.AbstractPileupBuilder;
import jacusa.pileup.iterator.location.AbstractLocationAdvancer;
import jacusa.pileup.iterator.variant.Variant;
import jacusa.util.Coordinate;
import jacusa.util.Location;
import net.sf.samtools.SAMFileReader;

public abstract class AbstractTwoSampleIterator extends AbstractWindowIterator {

	// sample 1
	protected SampleParameters sample1;
	protected final AbstractPileupBuilder[] pileupBuilders1;	

	// sample 2
	protected SampleParameters sample2;
	protected final AbstractPileupBuilder[] pileupBuilders2;

	public AbstractTwoSampleIterator(
			final Coordinate coordinate,
			final Variant filter,
			final SAMFileReader[] readers1,
			final SAMFileReader[] readers2,
			final SampleParameters sample1,
			final SampleParameters sample2,
			AbstractParameters parameters) {
		super(coordinate, filter, parameters);

		this.sample1 = sample1;
		pileupBuilders1 = createPileupBuilders(
				sample1.getPileupBuilderFactory(), 
				coordinate, 
				readers1,
				sample1,
				parameters);
		final Location loc1 = initLocation(coordinate, sample1.getPileupBuilderFactory().isDirected(), pileupBuilders1);

		this.sample2 = sample2;
		pileupBuilders2 = createPileupBuilders(
				sample2.getPileupBuilderFactory(), 
				coordinate, 
				readers2,
				sample2,
				parameters);
		final Location loc2 = initLocation(coordinate, sample2.getPileupBuilderFactory().isDirected(), pileupBuilders2);
		
		// create the correct LocationAdvancer
		locationAdvancer = AbstractLocationAdvancer.getInstance(
				sample1.getPileupBuilderFactory().isDirected(), loc1, 
				sample2.getPileupBuilderFactory().isDirected(), loc2);

		parallelPileup = new DefaultParallelPileup(pileupBuilders1.length, pileupBuilders2.length);
	}

	protected boolean hasNext1() {
		return hasNext(locationAdvancer.getLocation1(), pileupBuilders1);
	}

	protected boolean hasNext2() {
		return hasNext(locationAdvancer.getLocation2(), pileupBuilders2);
	}
	
	public FilterContainer[] getFilterContainers4Replicates1(Location location) {
		return getFilterCaches4Replicates(location, pileupBuilders1);
	}

	public FilterContainer[] getFilterContainers4Replicates2(Location location) {
		return getFilterCaches4Replicates(location, pileupBuilders2);
	}
	
}