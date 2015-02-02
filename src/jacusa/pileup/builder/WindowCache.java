package jacusa.pileup.builder;

import jacusa.phred2prob.Phred2Prob;
import jacusa.util.WindowCoordinates;

import java.util.Arrays;

// TODO similar to base count -> try to merge or reuse code
public class WindowCache {

	private WindowCoordinates windowCoordinates;
	private int baseLength;

	private int[] coverage;
	private int[][] baseCount;
	private int[][][] qualCount;

	public WindowCache(final WindowCoordinates windowCoordinates, final int baseLength) {
		this.windowCoordinates 	= windowCoordinates;
		this.baseLength 		= baseLength;

		final int windowSize	= windowCoordinates.getWindowSize();
		coverage 				= new int[windowSize];
		baseCount 				= new int[windowSize][baseLength];
		qualCount 				= new int[windowSize][baseLength][Phred2Prob.MAX_Q];
	}

	public void clear() {
		Arrays.fill(coverage, 0);
		for (int windowI = 0; windowI < windowCoordinates.getWindowSize(); windowI++) {
			Arrays.fill(baseCount[windowI], 0);

			for (int baseI = 0; baseI < baseLength; ++baseI) {
				Arrays.fill(qualCount[windowI][baseI], 0);
			}
		}
	}

	public void add(final int windowPosition, final int baseI, int qual) {
		coverage[windowPosition]++;
		baseCount[windowPosition][baseI]++;
		// make sure we don't exceed...
		Math.min(Phred2Prob.MAX_Q - 1, qual);
		qualCount[windowPosition][baseI][qual]++;
	}

	public int getCoverage(final int windowPosition) {
		return coverage[windowPosition];
	}

	public int[] getBaseCount(final int windowPosition) {
		return baseCount[windowPosition];
	}
	
	public int[][] getQualCount(final int windowPosition) {
		return qualCount[windowPosition];
	}

	public int[] getBaseI(final int windowPosition) {
		return baseCount[windowPosition];
	}

	public int[][] getQual(final int windowPosition) {
		return qualCount[windowPosition];
	}

	public WindowCoordinates getWindowCoordinates() {
		return windowCoordinates;
	}

}