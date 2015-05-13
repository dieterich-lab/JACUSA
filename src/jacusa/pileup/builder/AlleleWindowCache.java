package jacusa.pileup.builder;

import java.util.Arrays;

import jacusa.util.WindowCoordinates;

public class AlleleWindowCache {

	private WindowCoordinates windowCoordinates;
	private int baseLength;

	private int[] coverage;
	private int[] alleles;
	private int[] mask;

	public AlleleWindowCache(final WindowCoordinates windowCoordinates, final int baseLength) {
		this.windowCoordinates 	= windowCoordinates;
		this.baseLength 		= baseLength;

		final int windowSize	= windowCoordinates.getWindowSize();
		coverage = new int[windowSize];
		alleles = new int[windowSize];
		mask = new int[windowSize];
	}

	public void clear() {
		Arrays.fill(coverage, 0);
		Arrays.fill(alleles, 0);
		Arrays.fill(mask, 0);
	}

	public void add(final int windowPosition, final int baseI, int qualI) {
		int r = 2 << baseI;
		int t = mask[windowPosition] & r;
		if (t == 0) {
			mask[windowPosition] += r;
			++alleles[windowPosition];
		}
	}
	
	public int getCoverage(final int windowPosition) {
		return coverage[windowPosition];
	}

	public int getBaseLength() {
		return baseLength;
	}
	
	public WindowCoordinates getWindowCoordinates() {
		return windowCoordinates;
	}

}