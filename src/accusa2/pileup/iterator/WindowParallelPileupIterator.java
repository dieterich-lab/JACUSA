package accusa2.pileup.iterator;

import net.sf.samtools.SAMFileReader;
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

	protected final AnnotatedCoordinate coordinate;
	
	// pileupBuilders
	protected final AbstractPileupBuilder[] pileupBuildersA;
	protected final AbstractPileupBuilder[] pileupBuildersB;

	protected int filters;

	// output
	protected ParallelPileup parallelPileup;

	public WindowParallelPileupIterator(final AnnotatedCoordinate annotatedCoordinate, final SAMFileReader[] readersA, final SAMFileReader[] readersB, final Parameters parameters) {
		this.coordinate = annotatedCoordinate;

		pileupBuildersA = createPileupBuilders(parameters.getPileupBuilderFactoryA(), annotatedCoordinate, readersA, parameters);
		pileupBuildersB = createPileupBuilders(parameters.getPileupBuilderFactoryB(), annotatedCoordinate, readersB, parameters);

		filters = 1; // TODO Parameters.getInstance().getFilterConfig();

		// init
		parallelPileup = new DefaultParallelPileup(0, 0);
		parallelPileup.setContig(annotatedCoordinate.getSequenceName());
		init(pileupBuildersA);
		init(pileupBuildersB);
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

		for (AbstractPileupBuilder pileupBuilder : pileupBuilders) {
			pileupBuilder.adjustWindowStart(nextValidGenomicPosition);
		}

		return nextValidGenomicPosition;
	}

	private int getNextValidGenomicPosition(int targetGenomicPosition, AbstractPileupBuilder[] pileupBuilders) {
		int nextValidPosition = -1;

		for (AbstractPileupBuilder pileupBuilder : pileupBuilders) {
			nextValidPosition = Math.max(nextValidPosition, pileupBuilder.getNextValidGenomicPosition(targetGenomicPosition));
		}

		return nextValidPosition;
	}

	private boolean hasNext(final AbstractPileupBuilder[] pileupBuilders) {
		int currentGenomicPosition = parallelPileup.getPosition();
		if (isCovered(currentGenomicPosition, pileupBuilders)) {
			return true;
		}
		
		// within
		while (currentGenomicPosition <= coordinate.getEnd()) {
			if (pileupBuilders[0].isContainedInWindow(currentGenomicPosition)) {
				if (isCovered(currentGenomicPosition, pileupBuilders)) {
					parallelPileup.setPosition(currentGenomicPosition);
					return true;
				} else {
					// move along the window
					++currentGenomicPosition;
				}
			} else {
				currentGenomicPosition = getNextValidGenomicPosition(currentGenomicPosition, pileupBuilders);
				if (currentGenomicPosition == -1) {
					return false;
				} if (! adjustWindowStart(currentGenomicPosition, pileupBuilders)) {
					return false;
				}
			}
		}

		return false;
	}

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
		Counts[][] counts = new Counts[n][filters];

		int windowPosition = pileupBuilders[0].convertGenomicPosition2WindowPosition(parallelPileup.getPosition());
		for(int i = 0; i < n; ++i) {
			counts[i] = pileupBuildersA[i].getFilteredCounts(windowPosition, STRAND.UNKNOWN);
			// TODO check if copy needed
		}

		return counts;
	}

	// TODO make this more quantitative
	protected boolean isVariant(ParallelPileup parallelPileup)  {
		return parallelPileup.getPooledPileup().getAlleles().length > 1;
	}

	private boolean adjustCurrentGenomicPosition(int targetGenomicPosition, AbstractPileupBuilder[] pileupBuilders) {
		boolean ret = false;

		if (! pileupBuilders[0].isContainedInWindow(targetGenomicPosition)) {
			ret = adjustWindowStart(targetGenomicPosition, pileupBuilders);
		}

		parallelPileup.setPosition(targetGenomicPosition);
		return ret;
	}

	private boolean adjustWindowStart(int genomicWindowStart, AbstractPileupBuilder[] pileupBuilders) {
		boolean ret = false;

		for (AbstractPileupBuilder pileupBuilder : pileupBuilders) {
			ret |= pileupBuilder.adjustWindowStart(genomicWindowStart);
		}

		return ret;
	}

	private Pileup[] complementPileups(Pileup[] pileups) {
		Pileup[] complementedPileups = new DefaultPileup[pileups.length];
		for(int i = 0; i < pileups.length; ++i) {
			complementedPileups[i] = pileups[i].complement();
		}
		return complementedPileups;
	}

	public boolean hasNext() {
		if (parallelPileup.isValid()) {
			return true;
		}

		while (hasNext(pileupBuildersA) && hasNext(pileupBuildersB)) {
			final int positionA = parallelPileup.getPooledPileupA().getPosition();
			final int positionB = parallelPileup.getPooledPileupB().getPosition();

			final int compare = new Integer(positionA).compareTo(positionB);

			switch (compare) {

			case -1:
				// adjust actualPosition; instead of iterating jump to specific
				// position
				adjustCurrentGenomicPosition(positionB, pileupBuildersA);
				if (hasNext(pileupBuildersA)) {
					parallelPileup.setPileupsA(getPileups(parallelPileup.getPosition(), pileupBuildersA));
				} else { 
					return false; 
				}
				break;

			case 0:

				final boolean isVariant = isVariant(parallelPileup);

				if(isVariant) {
					return true;
				} else {
					advance();
				}
				break;

			case 1:
				// adjust actualPosition; instead of iterating jump to specific
				// position
				adjustCurrentGenomicPosition(positionA, pileupBuildersB);
				if (hasNext(pileupBuildersB)) {
					parallelPileup.setPileupsB(getPileups(parallelPileup.getPosition(), pileupBuildersB));
				} else { 
					return false; 
				}
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

	
	public DefaultParallelPileup next() {
		if (!hasNext()) {
			return null;
		}

		DefaultParallelPileup parallelPileup = new DefaultParallelPileup(this.parallelPileup);
		parallelPileup.setFilterCountsA(getCounts(pileupBuildersA));
		parallelPileup.setFilterCountsB(getCounts(pileupBuildersB));

		// advance to the next position
		advance();

		return parallelPileup;
	}

	private void advance() {
		parallelPileup.setPosition(parallelPileup.getPosition() + 1);
	}

	/*
	public DefaultParallelPileup next() {
		if (!hasNext()) {
			return null;
		}

		DefaultParallelPileup ret = new DefaultParallelPileup(parallelPileup);
		ret.setFilterCountsA(getCounts(pileupBuildersA));
		ret.setFilterCountsB(getCounts(pileupBuildersB));

		// TODO check
		// this is necessary!!!
		if(parallelPileup.getPooledPileupA().getStrand() == STRAND.UNKNOWN && parallelPileup.getPooledPileupB().getStrand() == STRAND.FORWARD) {
			parallelPileup.setPileupsB(new DefaultPileup[0]);
		} else if(parallelPileup.getPooledPileupB().getStrand() == STRAND.UNKNOWN && parallelPileup.getPooledPileupA().getStrand() == STRAND.FORWARD) {
			parallelPileup.setPileupsA(new DefaultPileup[0]);
		} else {
			parallelPileup.setPileupsA(new DefaultPileup[0]);
			parallelPileup.setPileupsB(new DefaultPileup[0]);
		}

		return ret;
	}
	*/

	public final AnnotatedCoordinate getAnnotatedCoordinate() {
		return coordinate;
	}

	@Override
	public final void remove() {
		// not needed
	}

}