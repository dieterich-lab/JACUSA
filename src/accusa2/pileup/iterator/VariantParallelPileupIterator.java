package accusa2.pileup.iterator;


import net.sf.samtools.SAMFileReader;

import accusa2.cli.Parameters;
import accusa2.pileup.Pileup.STRAND;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;
import accusa2.pileup.builder.AbstractPileupBuilder;
import accusa2.pileup.builder.PileupBuilderFactory;
import accusa2.util.AnnotatedCoordinate;

/**
 * @author Michael Piechotta
 */
public class VariantParallelPileupIterator implements ParallelPileupIterator {

	protected final AnnotatedCoordinate coordinate;
	
	protected final AbstractPileupBuilder[] pileupBuilders1;
	protected final AbstractPileupBuilder[] pileupBuilders2;

	protected ParallelPileup parallelPileup;

	public VariantParallelPileupIterator(final AnnotatedCoordinate annotatedCoordinate, final SAMFileReader[] readers1, final SAMFileReader[] readers2, final Parameters parameters) {
		this.coordinate = annotatedCoordinate;

		pileupBuilders1 = create(parameters.getPileupBuilderFactory1(), annotatedCoordinate, readers1, parameters);
		pileupBuilders2 = create(parameters.getPileupBuilderFactory2(), annotatedCoordinate, readers2, parameters);

		// init
		parallelPileup = new ParallelPileup(0, 0);
	}

	private AbstractPileupBuilder[] create(final PileupBuilderFactory pileupBuilderFactory, final AnnotatedCoordinate annotatedCoordinate, final SAMFileReader[] readers, final Parameters parameters) {
		AbstractPileupBuilder[] pileupBuilders = new AbstractPileupBuilder[readers.length];
		for(int i = 0; i < readers.length; ++i) {
			pileupBuilders[i] = pileupBuilderFactory.newInstance(annotatedCoordinate, readers[i], parameters);
		}
		return pileupBuilders;
	}

	/**
	 * 
	 */
	private boolean hasNext(AbstractPileupBuilder[] pileupBuilders) {
		boolean hasNext = false;

		for(AbstractPileupBuilder pileupBuilder : pileupBuilders) {
			hasNext |= pileupBuilder.hasNext();
		}

		return hasNext;
	}
	
	/**
	 * 
	 */
	public boolean hasNext() {
		if(parallelPileup.isValid()) {
			return true;
		}

		// quit if there are no pileups to build
		if(!hasNext(pileupBuilders1) || !hasNext(pileupBuilders2)) {
			return false;
		}
		// init
		if(parallelPileup.getN1() == 0) {
			parallelPileup.setPileups1(next(pileupBuilders1));
		}
		// init
		if(parallelPileup.getN2() == 0) {
			parallelPileup.setPileups2(next(pileupBuilders2));
		}

		return findNext();
	}

	private Pileup[] next(AbstractPileupBuilder[] pileupBuilders) {
		int n = pileupBuilders.length;
		Pileup[] pileups = new Pileup[n];
		for(int i = 0; i < n; ++i) {
			pileups[i] = pileupBuilders[i].next();
		}

		return pileups;
	}
	
	protected boolean isVariant(ParallelPileup parallelPileup)  {
		return parallelPileup.getPooledPileup().getAlleles().length > 1;
	}

	private void adjustCurrentGenomicPosition(AbstractPileupBuilder[] pileupBuilders, int position) {
		for(AbstractPileupBuilder pileupBuilder : pileupBuilders) {
			pileupBuilder.adjustCurrentGenomicPosition(position);
		}
	}

	private Pileup[] complementPileups(Pileup[] pileups) {
		Pileup[] complementedPileups = new Pileup[pileups.length];
		for(int i = 0; i < pileups.length; ++i) {
			complementedPileups[i] = pileups[i].complement();
		}
		return complementedPileups;
	}

	protected boolean findNext() {
		while (parallelPileup.isValid()) {
			int position1 = parallelPileup.getPooledPileup1().getPosition();
			int position2 = parallelPileup.getPooledPileup2().getPosition();
			
			final int compare = new Integer(position1).compareTo(position2);

			switch (compare) {

			case -1:
				// adjust actualPosition; instead of iterating jump to specific
				// position
				adjustCurrentGenomicPosition(pileupBuilders1, position2);
				if(hasNext(pileupBuilders1)) {
					parallelPileup.setPileups1(next(pileupBuilders1));
				} else {
					parallelPileup.setPileups1(new Pileup[0]);
				}
				break;

			case 0:

				final STRAND strand1 = parallelPileup.getStrand1();
				final STRAND strand2 = parallelPileup.getStrand2();
				/*
				 * UGLY code!!
				 * Do not update strand{1,2}, when pileups are changed/complemented in the following,
				 * otherwise "UGLY code continued" won't work as expected 
				 */
				// change parallelPileup if U,S or S,U encountered
				if(strand1 == STRAND.UNKNOWN && strand2 == STRAND.REVERSE) {
					parallelPileup.setPileups1(complementPileups(parallelPileup.getPileups1()));
				}
				if(strand2 == STRAND.UNKNOWN && strand1 == STRAND.REVERSE) {
					parallelPileup.setPileups2(complementPileups(parallelPileup.getPileups2()));
				}
				final boolean isVariant = isVariant(parallelPileup);

				if(isVariant && strand1 == strand2) {
					return true;
					/*
					 * UGLY code continued!
					 */
				} else if(isVariant && (strand1 == STRAND.UNKNOWN || strand2 == STRAND.UNKNOWN)) {
					return true;
				} else if(strand1 == STRAND.REVERSE) {
					if(hasNext(pileupBuilders2)) {
						parallelPileup.setPileups2(next(pileupBuilders2));
					} else {
						parallelPileup.setPileups2(new Pileup[0]);
					}
				} else if(strand2 == STRAND.REVERSE) {
					if(hasNext(pileupBuilders1)) {
						parallelPileup.setPileups1(next(pileupBuilders1));
					} else {
						parallelPileup.setPileups1(new Pileup[0]);
					}					
				} else {
					if(hasNext(pileupBuilders1)) {
						parallelPileup.setPileups1(next(pileupBuilders1));
					} else {
						parallelPileup.setPileups1(new Pileup[0]);
					}
					if(hasNext(pileupBuilders2)) {
						parallelPileup.setPileups2(next(pileupBuilders2));
					} else {
						parallelPileup.setPileups2(new Pileup[0]);
					}
				}
				break;

			case 1:
				// adjust actualPosition; instead of iterating jump to specific
				// position
				adjustCurrentGenomicPosition(pileupBuilders2, position1);
				if(hasNext(pileupBuilders2)) {
					parallelPileup.setPileups2(next(pileupBuilders2));
				} else {
					parallelPileup.setPileups2(new Pileup[0]);
				}
				break;
			}
		}

		return false;
	}

	public ParallelPileup next() {
		if (!hasNext()) {
			return null;
		}
		
		ParallelPileup ret = new ParallelPileup(parallelPileup);

		// this is necessary!!!
		if(parallelPileup.getPooledPileup1().getStrand() == STRAND.UNKNOWN && parallelPileup.getPooledPileup2().getStrand() == STRAND.FORWARD) {
			parallelPileup.setPileups2(new Pileup[0]);
		} else if(parallelPileup.getPooledPileup2().getStrand() == STRAND.UNKNOWN && parallelPileup.getPooledPileup1().getStrand() == STRAND.FORWARD) {
			parallelPileup.setPileups1(new Pileup[0]);
		} else {
			parallelPileup.setPileups1(new Pileup[0]);
			parallelPileup.setPileups2(new Pileup[0]);
		}

		return ret;
	}

	public final AnnotatedCoordinate getAnnotatedCoordinate() {
		return coordinate;
	}
	
	@Override
	public final void remove() {
		// not needed
	}

}
