package accusa2.pileup.iterator;

import net.sf.samtools.SAMFileReader;
import accusa2.cli.parameters.AbstractParameters;
import accusa2.pileup.DefaultPileup;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;
import accusa2.pileup.DefaultPileup.Counts;
import accusa2.pileup.DefaultPileup.STRAND;
import accusa2.util.AnnotatedCoordinate;

public class StrandedVariantParallelPileupWindowIterator extends AbstractParallelPileupWindowIterator {

	public StrandedVariantParallelPileupWindowIterator(
			final AnnotatedCoordinate annotatedCoordinate, 
			final SAMFileReader[] readersA, 
			final SAMFileReader[] readersB, 
			final AbstractParameters parameters) {
		super(annotatedCoordinate, readersA, readersB, parameters);
	}

	@Override
	protected int advance(int currentGenomicPosition, STRAND strand) {
		switch (strand) {
		case FORWARD:
			strand = STRAND.REVERSE;
			break;
		
		case REVERSE:
			strand = STRAND.FORWARD;
			currentGenomicPosition++;
		
		case UNKNOWN:
		default:
			currentGenomicPosition++;
			break;
		}

		return currentGenomicPosition;
	}

	@Override
	protected boolean isVariant(ParallelPileup parallelPileup)  {
		return parallelPileup.getPooledPileup().getAlleles().length > 1;
	}

	@Override
	public ParallelPileup next() {
		if (! hasNext()) {
			return null;
		}

		if(strandA == STRAND.UNKNOWN && strandB == STRAND.REVERSE) {
			parallelPileup.setFilterCountsA(complementCounts(getCounts(genomicPositionA, strandB, pileupBuildersA)));
		} else {
			parallelPileup.setFilterCountsA(getCounts(genomicPositionA, strandB, pileupBuildersA));
		}

		if(strandB == STRAND.UNKNOWN && strandA == STRAND.REVERSE) {
			parallelPileup.setFilterCountsB(complementCounts(getCounts(genomicPositionB, strandB, pileupBuildersB)));
		} else {
			parallelPileup.setFilterCountsB(getCounts(genomicPositionB, strandB, pileupBuildersB));
		}

		// advance to the next position
		advance();

		return parallelPileup;
	}
	
	@Override
	public boolean hasNext() {
		while (hasNextA() && hasNextB()) {
			final int compare = new Integer(genomicPositionA).compareTo(genomicPositionB);

			switch (compare) {

			case -1:
				// adjust actualPosition; instead of iterating jump to specific
				// position
				adjustCurrentGenomicPosition(genomicPositionB, pileupBuildersA);
				genomicPositionA = genomicPositionB;
				break;

			case 0:
				final STRAND strandA = parallelPileup.getStrandA();
				final STRAND strandB = parallelPileup.getStrandB();

				parallelPileup.setPosition(genomicPositionA);
				
				// complement bases if one sample is unstranded and 
				// the other is stranded and maps to the opposite strand
				parallelPileup.setPileupsA(getPileups(genomicPositionA, strandA, pileupBuildersA));
				if(strandA == STRAND.UNKNOWN && strandB == STRAND.REVERSE) {
					parallelPileup.setPileupsA(complementPileups(parallelPileup.getPileupsA()));
				}
				parallelPileup.setPileupsB(getPileups(genomicPositionB, strandB, pileupBuildersB));
				if(strandB == STRAND.UNKNOWN && strandA == STRAND.REVERSE) {
					parallelPileup.setPileupsB(complementPileups(parallelPileup.getPileupsB()));
				}

				final boolean isVariant = isVariant(parallelPileup);
				if (isVariant) {
					return true;
				} else {
					advance();
				}
				break;

			case 1:
				// adjust actualPosition; instead of iterating jump to specific
				// position
				adjustCurrentGenomicPosition(genomicPositionA, pileupBuildersB);
				genomicPositionB = genomicPositionA;
				break;
			}
		}

		return false;
	}

	protected void advance() {
		if (strandA == STRAND.UNKNOWN) {
			if (strandB == STRAND.UNKNOWN || strandB == STRAND.REVERSE) {
				genomicPositionA++;
				genomicPositionB++;
			} else if (strandB == STRAND.FORWARD){
				strandB = STRAND.REVERSE;
			}
		}
		if (strandB == STRAND.UNKNOWN) {
			if (strandA == STRAND.REVERSE) {
				genomicPositionA++;
				genomicPositionB++;
			} else if (strandA == STRAND.FORWARD){
				strandA = STRAND.REVERSE;
			}
		}
		if (strandA == STRAND.FORWARD && strandB == STRAND.FORWARD) {
			strandA = STRAND.REVERSE;
			strandB = STRAND.REVERSE;
		} else {
			genomicPositionA++;
			genomicPositionB++;
		}
	}
	
	protected Pileup[] complementPileups(Pileup[] pileups) {
		Pileup[] complementedPileups = new DefaultPileup[pileups.length];

		for (int i = 0; i < pileups.length; ++i) {
			complementedPileups[i] = pileups[i].complement();
		}

		return complementedPileups;
	}
	
	protected Counts[][] complementCounts(Counts[][] counts) {
		int replicates = counts.length;
		Counts[][] complementedCounts = new Counts[replicates][filterCount]; // FIXME

		for (int i = 0; i < replicates; ++i) {
			for (int j = 0; j < filterCount; ++j) {
				complementedCounts[i][j] = counts[i][j]; // TODO check
				complementedCounts[i][j].invertCounts();
			}
		}

		return complementedCounts;
	}

}