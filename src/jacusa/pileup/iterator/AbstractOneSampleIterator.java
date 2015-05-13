package jacusa.pileup.iterator;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.filter.FilterContainer;
import jacusa.pileup.DefaultPileup;
import jacusa.pileup.Pileup;
import jacusa.pileup.builder.AbstractPileupBuilder;
import jacusa.pileup.iterator.variant.Variant;
import jacusa.util.Coordinate;
import jacusa.util.Location;
import net.sf.samtools.SAMFileReader;

@Deprecated
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

	protected Pileup[] removeBase(int baseI, Pileup[] pileups) {
		int n = pileups.length;
		Pileup[] ret = new Pileup[n];

		for (int i = 0; i < n; ++i) {
			Pileup pileup = new DefaultPileup(pileups[i]);

			pileup.getCounts().substract(baseI, pileups[i].getCounts());
			ret[i] = pileup;
		}

		return ret;
	}

	// For one sample this should -1
	protected int getHomomorphBaseI(Pileup pooled) {
		int baseI = -1;
		
		return baseI;
	}

	public FilterContainer[] getFilterContainers4Replicates1(Location location) {
		return getFilterCaches4Replicates(location, pileupBuilders);
	}

	// ugly code getFilterContainers4Replicates2(Location location)
			// should be defined as getFilterContainers4Replicates(int sample, Location location)
	public FilterContainer[] getFilterContainers4Replicates2(Location location) {
		return null; 
	}
	
}