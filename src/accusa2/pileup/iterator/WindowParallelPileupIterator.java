package accusa2.pileup.iterator;

import net.sf.samtools.SAMFileReader;
import accusa2.ACCUSA2;
import accusa2.cli.Parameters;
import accusa2.pileup.DefaultParallelPileup;
import accusa2.pileup.DefaultPileup;
import accusa2.pileup.DefaultPileup.Counts;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;
import accusa2.pileup.DefaultPileup.STRAND;
import accusa2.pileup.builder.AbstractPileupBuilder;
import accusa2.pileup.builder.PileupBuilderFactory;
import accusa2.util.AnnotatedCoordinate;

public class WindowParallelPileupIterator implements ParallelPileupIterator {

	protected int genomicPositionA;
	protected int genomicPositionB;

	protected final AnnotatedCoordinate coordinate;
	
	// pileupBuilders
	protected final AbstractPileupBuilder[] pileupBuildersA;
	protected final AbstractPileupBuilder[] pileupBuildersB;

	protected int filterCount;

	// output
	protected ParallelPileup parallelPileup;

	public WindowParallelPileupIterator(final AnnotatedCoordinate annotatedCoordinate, final SAMFileReader[] readersA, final SAMFileReader[] readersB, final Parameters parameters) {
		this.coordinate = annotatedCoordinate;

		pileupBuildersA = createPileupBuilders(parameters.getPileupBuilderFactoryA(), annotatedCoordinate, readersA, parameters);
		pileupBuildersB = createPileupBuilders(parameters.getPileupBuilderFactoryB(), annotatedCoordinate, readersB, parameters);

		filterCount = parameters.getFilterConfig().getFactories().size();

		// init
		parallelPileup = new DefaultParallelPileup(pileupBuildersA.length, pileupBuildersB.length);
		parallelPileup.setContig(annotatedCoordinate.getSequenceName());

		genomicPositionA = init(pileupBuildersA);
		genomicPositionB = init(pileupBuildersB);
	}

	/**
	 * 
	 * @param pileupBuilderFactory
	 * @param annotatedCoordinate
	 * @param readers
	 * @param parameters
	 * @return
	 */
	private AbstractPileupBuilder[] createPileupBuilders(final PileupBuilderFactory pileupBuilderFactory, final AnnotatedCoordinate annotatedCoordinate, final SAMFileReader[] readers, final Parameters parameters) {
		AbstractPileupBuilder[] pileupBuilders = new AbstractPileupBuilder[readers.length];

		for(int i = 0; i < readers.length; ++i) {
			pileupBuilders[i] = pileupBuilderFactory.newInstance(annotatedCoordinate, readers[i], parameters);
		}

		return pileupBuilders;
	}

	private int init(AbstractPileupBuilder[] pileupBuilders) {
		int nextValidGenomicPosition = getNextValidGenomicPosition(coordinate.getStart(), pileupBuilders);

		if (nextValidGenomicPosition > 0) {
			for (AbstractPileupBuilder pileupBuilder : pileupBuilders) {
				pileupBuilder.adjustWindowStart(nextValidGenomicPosition);
			}
		}

		return nextValidGenomicPosition;
	}

	// TODO at least two need to be covered
	private int getNextValidGenomicPosition(int targetGenomicPosition, AbstractPileupBuilder[] pileupBuilders) {
		int nextValidPosition = -1;

		for (AbstractPileupBuilder pileupBuilder : pileupBuilders) {
			nextValidPosition = pileupBuilder.getNextValidGenomicPosition(targetGenomicPosition);
			if (nextValidPosition > 0) {
				return nextValidPosition;
			}
		}

		return nextValidPosition;
	}

	private boolean hasNextA() {
		int newGenomicPosition = hasNext(genomicPositionA, pileupBuildersA);
		if (newGenomicPosition < 0) {
			return false;
		}

		genomicPositionA = newGenomicPosition;
		return true;
	}

	private boolean hasNextB() {
		int newGenomicPosition = hasNext(genomicPositionB, pileupBuildersB);
		if (newGenomicPosition < 0) {
			return false;
		}
		
		genomicPositionB = newGenomicPosition;
		return true;
	}
	
	private int hasNext(int currentGenomicPosition, final AbstractPileupBuilder[] pileupBuilders) {
		// within
		while (currentGenomicPosition <= coordinate.getEnd()) {
			if (pileupBuilders[0].isContainedInWindow(currentGenomicPosition)) {
				if (isCovered(currentGenomicPosition, pileupBuilders)) {
					return currentGenomicPosition;
				} else {
					// move along the window
					++currentGenomicPosition;
				}
			} else {
				currentGenomicPosition = getNextValidGenomicPosition(currentGenomicPosition, pileupBuilders);
				if (currentGenomicPosition == -1) {
					return -1;
				} if (! adjustWindowStart(currentGenomicPosition, pileupBuilders)) {
					return -1;
				}
			}
		}

		return -1;
	}

	// TODO make this more quantitative
	private boolean isCovered(int genomicPosition, AbstractPileupBuilder[] pileupBuilders) {
		int windowPosition = pileupBuilders[0].convertGenomicPosition2WindowPosition(genomicPosition);

		for (AbstractPileupBuilder pileupBuilder : pileupBuilders) {
			if (! pileupBuilder.isCovered(windowPosition)) {
				return false;
			}
		}

		return true;
	}

	private Pileup[] getPileups(int genomicPosition, AbstractPileupBuilder[] pileupBuilders) {
		int n = pileupBuilders.length;
		Pileup[] pileups = new DefaultPileup[n];

		int windowPosition = pileupBuilders[0].convertGenomicPosition2WindowPosition(genomicPosition);
		for(int i = 0; i < n; ++i) {
			pileups[i] = pileupBuilders[i].getPileup(windowPosition, STRAND.UNKNOWN);
		}

		return pileups;
	}

	private Counts[][] getCounts(AbstractPileupBuilder[] pileupBuilders) {
		int n = pileupBuilders.length;
		Counts[][] counts = new Counts[n][filterCount];

		int windowPosition = pileupBuilders[0].convertGenomicPosition2WindowPosition(parallelPileup.getPosition());
		for(int i = 0; i < n; ++i) {
			counts[i] = pileupBuildersA[i].getFilteredCounts(windowPosition, STRAND.UNKNOWN);
		}

		return counts;
	}

	protected boolean isVariant(ParallelPileup parallelPileup)  {
		return parallelPileup.getPooledPileup().getAlleles().length > 0;
	}

	private boolean adjustCurrentGenomicPosition(int targetGenomicPosition, AbstractPileupBuilder[] pileupBuilders) {
		boolean ret = false;

		if (! pileupBuilders[0].isContainedInWindow(targetGenomicPosition)) {
			ret = adjustWindowStart(targetGenomicPosition, pileupBuilders);
		}

		return ret;
	}

	private boolean adjustWindowStart(int genomicWindowStart, AbstractPileupBuilder[] pileupBuilders) {
		boolean ret = false;

		for (AbstractPileupBuilder pileupBuilder : pileupBuilders) {
			ret |= pileupBuilder.adjustWindowStart(genomicWindowStart);
		}

		return ret;
	}

	/*
	private Pileup[] complementPileups(Pileup[] pileups) {
		Pileup[] complementedPileups = new DefaultPileup[pileups.length];
		for(int i = 0; i < pileups.length; ++i) {
			complementedPileups[i] = pileups[i].complement();
		}
		return complementedPileups;
	}
	*/

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
				parallelPileup.setPosition(genomicPositionA);
				parallelPileup.setPileupsA(getPileups(genomicPositionA, pileupBuildersA));
				parallelPileup.setPileupsB(getPileups(genomicPositionB, pileupBuildersB));

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

	/*
	protected boolean findNext() {
		while (parallelPileup.isValid()) {
			int positionA = parallelPileup.getPooledPileupA().getPosition();
			int positionB = parallelPileup.getPooledPileupB().getPosition();

			final int compare = new Integer(positionA).compareTo(positionB);

			switch (compare) {

			case -1:
				// adjust actualPosition; instead of iterating jump to specific
				// position
				adjustCurrentGenomicPosition(positionB, pileupBuildersA);
				if (hasNext(pileupBuildersA)) {
					parallelPileup.setPileupsA(getPileups(pileupBuildersA));
				} else {
					parallelPileup.setPileupsA(new DefaultPileup[0]);
					return false;
				}
				break;

			case 0:

				final STRAND strandA = parallelPileup.getStrandA();
				final STRAND strandB = parallelPileup.getStrandB();
				/*
				 * UGLY code!!
				 * Do not update strand{1,2}, when pileups are changed/complemented in the following,
				 * otherwise "UGLY code continued" won't work as expected 
				 *
				// change parallelPileup if U,S or S,U encountered
				if(strandA == STRAND.UNKNOWN && strandB == STRAND.REVERSE) {
					parallelPileup.setPileupsA(complementPileups(parallelPileup.getPileupsA()));
				}
				if(strandB == STRAND.UNKNOWN && strandA == STRAND.REVERSE) {
					parallelPileup.setPileupsB(complementPileups(parallelPileup.getPileupsB()));
				}
				final boolean isVariant = isVariant(parallelPileup);

				if(isVariant && strandA == strandB) {
					return true;
					/*
					 * UGLY code continued!
					 *
				} else if(isVariant && (strandA == STRAND.UNKNOWN || strandB == STRAND.UNKNOWN)) {
					return true;
				} else if(strandA == STRAND.REVERSE) {
					if(hasNext(pileupBuildersB)) {
						parallelPileup.setPileupsB(getPileups(pileupBuildersB));
					} else {
						parallelPileup.setPileupsB(new DefaultPileup[0]);
					}
				} else if(strandB == STRAND.REVERSE) {
					if(hasNext(pileupBuildersA)) {
						parallelPileup.setExtendedPileupsA(next(pileupBuildersA));
					} else {
						parallelPileup.setPileupsA(new DefaultPileup[0]);
					}					
				} else {
					if(hasNext(pileupBuildersA)) {
						parallelPileup.setExtendedPileupsA(next(pileupBuildersA));
					} else {
						parallelPileup.setPileupsA(new DefaultPileup[0]);
					}
					if(hasNext(pileupBuildersB)) {
						parallelPileup.setExtendedPileupsB(next(pileupBuildersB));
					} else {
						parallelPileup.setPileupsB(new DefaultPileup[0]);
					}
				}
				break;

			case 1:
				// adjust actualPosition; instead of iterating jump to specific
				// position
				adjustCurrentGenomicPosition(positionA, pileupBuildersB);
				if(hasNext(pileupBuildersB)) {
					parallelPileup.setPileupsB(getPileups(pileupBuildersB));
				} else {
					parallelPileup.setPileupsB(new DefaultPileup[0]);
					return false;
				}
				break;
			}
		}

		// return false;
	}
	*/
	
	public ParallelPileup next() {
		if (! hasNext()) {
			return null;
		}

		parallelPileup.setFilterCountsA(getCounts(pileupBuildersA));
		parallelPileup.setFilterCountsB(getCounts(pileupBuildersB));

		// advance to the next position
		advance();

if (parallelPileup.getPosition() % 10000 <= 1000) {
	ACCUSA2.printLog(Integer.toString(parallelPileup.getPosition()));
}

		return parallelPileup;
	}

	private void advance() {
		genomicPositionA++;
		genomicPositionB++;
	}

	public final AnnotatedCoordinate getAnnotatedCoordinate() {
		return coordinate;
	}

	@Override
	public final void remove() {
		// not needed
	}

}