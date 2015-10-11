package jacusa.filter;

import jacusa.cli.parameters.SampleParameters;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Pileup;
import jacusa.pileup.Result;
import jacusa.pileup.iterator.AbstractWindowIterator;
import jacusa.util.Location;

public class MaxDepthStorageFilter extends AbstractStorageFilter<Void> {

	private SampleParameters sampleParameters1;
	private SampleParameters sampleParameters2;
	
	public MaxDepthStorageFilter(
			final char c, 
			final SampleParameters sampleParameters1, 
			final SampleParameters sampleParameters2) {
		super(c);
		this.sampleParameters1 = sampleParameters1;
		this.sampleParameters2 = sampleParameters2;
	}

	@Override
	public boolean filter(final Result result, final Location location,	final AbstractWindowIterator windowIterator) {
		final ParallelPileup pp = result.getParellelPileup();
		
		if (sampleParameters1 != null) {
			if (filter(sampleParameters1.getMaxDepth(), pp.getPileups1())) {
				return true;
			}
		}
		
		if (sampleParameters2 != null) {
			if (filter(sampleParameters2.getMaxDepth(), pp.getPileups2())) {
				return true;
			}
		}
		
		return false;
	}

	private boolean filter(int maxDepth, Pileup[] pileups) {
		if (maxDepth > 0) { 
			for (final Pileup pileup : pileups) {
				if (pileup.getCoverage() > maxDepth) {
					return true;
				}
			}
		}
		
		return false;
	}
	
}