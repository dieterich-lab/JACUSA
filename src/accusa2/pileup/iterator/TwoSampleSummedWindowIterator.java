package accusa2.pileup.iterator;

import net.sf.samtools.SAMFileReader;
import accusa2.cli.parameters.AbstractParameters;
import accusa2.cli.parameters.SampleParameters;
import accusa2.pileup.DefaultPileup.Counts;
import accusa2.pileup.DefaultParallelPileup;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;
import accusa2.pileup.WindowedPileup;
import accusa2.pileup.builder.AbstractPileupBuilder;
import accusa2.pileup.iterator.variant.Variant;
import accusa2.util.AnnotatedCoordinate;

// TODO test
public class TwoSampleSummedWindowIterator extends AbstractWindowIterator {

	private TwoSampleUnstrandedIterator twoSampleUnstrandedIterator;
	private ParallelPileup parallelPileup;

	public TwoSampleSummedWindowIterator(
			final AnnotatedCoordinate annotatedCoordinate, 
			final Variant filter,
			final SAMFileReader[] readersA, 
			final SAMFileReader[] readersB,
			final SampleParameters sampleA,
			final SampleParameters sampleB,
			final AbstractParameters parameters) {
		super(annotatedCoordinate, filter, parameters);
		twoSampleUnstrandedIterator = new TwoSampleUnstrandedIterator(annotatedCoordinate, filter, readersA, readersB, sampleA, sampleB, parameters);
		parallelPileup = null;
	}

	public boolean hasNext() {
		if (parallelPileup == null && twoSampleUnstrandedIterator.hasNext()) {
			int nA = twoSampleUnstrandedIterator.pileupBuildersA.length;
			int nB = twoSampleUnstrandedIterator.pileupBuildersB.length;
			parallelPileup = new DefaultParallelPileup(nA, nB);
		} else {
			return false;
		}

		while (parallelPileup == null && twoSampleUnstrandedIterator.hasNext()) {
			ParallelPileup tmpParallelPileup = twoSampleUnstrandedIterator.next();

			// copy A
			for (int replicateI = 0; replicateI < tmpParallelPileup.getNA(); ++replicateI) {
				Pileup pileup = tmpParallelPileup.getPileupsA()[replicateI];
				parallelPileup.getPileupsA()[replicateI].addPileup(pileup);
			}

			// copy B
			for (int replicateI = 0; replicateI < tmpParallelPileup.getNB(); ++replicateI) {
				Pileup pileup = tmpParallelPileup.getPileupsB()[replicateI];
				parallelPileup.getPileupsB()[replicateI].addPileup(pileup);
			}

			return false;
		}

		return false;
	}

	public ParallelPileup next() {
		// TODO make more efficient
		ParallelPileup ret = new DefaultParallelPileup(parallelPileup);
		parallelPileup = null;

		return ret;
	}

	protected void advance(Location location) {
		// NOTHING to be done
	}

	protected boolean hasNext(Location location, final AbstractPileupBuilder[] pileupBuilders) {
		return false; // NOTHING to be done 
	}

	@Override
	protected WindowedPileup[] getPileups(Location location, AbstractPileupBuilder[] pileupBuilders) {
		return null; // NOTHING to be done
	}

	@Override
	protected Counts[][] getCounts(Location location, AbstractPileupBuilder[] pileupBuilders) {
		return null; // NOTHING to be done
	}

	@Override
	protected void advance() {
		// NOTHING to be done
	}

}