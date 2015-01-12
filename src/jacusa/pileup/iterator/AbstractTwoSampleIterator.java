package jacusa.pileup.iterator;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.filter.FilterContainer;
import jacusa.pileup.DefaultParallelPileup;
import jacusa.pileup.builder.AbstractPileupBuilder;
import jacusa.pileup.iterator.variant.Variant;
import jacusa.util.Coordinate;
import jacusa.util.Location;
import net.sf.samtools.SAMFileReader;

public abstract class AbstractTwoSampleIterator extends AbstractWindowIterator {

	// sample 1
	protected SampleParameters sample1;
	protected Location location1;
	protected final AbstractPileupBuilder[] pileupBuilders1;	

	// sample 2
	protected SampleParameters sample2;
	protected Location location2;
	protected final AbstractPileupBuilder[] pileupBuilders2;

	public AbstractTwoSampleIterator(
			final Coordinate annotatedCoordinate,
			final Variant filter,
			final SAMFileReader[] readersA,
			final SAMFileReader[] readersB,
			final SampleParameters sample1,
			final SampleParameters sample2,
			AbstractParameters parameters) {
		super(annotatedCoordinate, filter, parameters);

		this.sample1 = sample1;
		pileupBuilders1 = createPileupBuilders(
				sample1.getPileupBuilderFactory(), 
				annotatedCoordinate, 
				readersA,
				sample1,
				parameters);
		location1 = initLocation(annotatedCoordinate, sample1.getPileupBuilderFactory().isDirected(), pileupBuilders1);

		this.sample2 = sample2;
		pileupBuilders2 = createPileupBuilders(
				sample2.getPileupBuilderFactory(), 
				annotatedCoordinate, 
				readersB,
				sample2,
				parameters);
		location2 = initLocation(annotatedCoordinate, sample2.getPileupBuilderFactory().isDirected(), pileupBuilders2);

		parallelPileup = new DefaultParallelPileup(pileupBuilders1.length, pileupBuilders2.length);
	}

	protected boolean hasNext1() {
		return hasNext(location1, pileupBuilders1);
	}

	protected boolean hasNext2() {
		return hasNext(location2, pileupBuilders2);
	}
	
	public FilterContainer[] getFilterContainers4Replicates1(Location location) {
		return getFilterCaches4Replicates(location, pileupBuilders1);
	}

	public FilterContainer[] getFilterContainers4Replicates2(Location location) {
		return getFilterCaches4Replicates(location, pileupBuilders2);
	}

}