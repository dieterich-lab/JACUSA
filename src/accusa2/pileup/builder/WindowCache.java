package accusa2.pileup.builder;

import java.util.Arrays;

import accusa2.cli.Parameters;
import accusa2.process.phred2prob.Phred2Prob;

public class WindowCache {

	protected int windowsSize;
	protected int baseLength;
	
	protected int[] coverage;
	protected int[][] baseCount;
	protected int[][][] qualCount;
	
	protected int minQualI;
	
	public WindowCache(int windowSize, int baseLength) {
		this.windowsSize = windowSize;
		this.baseLength = baseLength;

		coverage 	= new int[windowSize];
		baseCount 	= new int[windowSize][baseLength];
		qualCount 	= new int[windowSize][baseLength][Phred2Prob.MAX_Q];

		minQualI 	= Parameters.getInstance().getMinBASQ();
	}

	public void clear() {
		Arrays.fill(coverage, 0);
		for (int windowI = 0; windowI < windowsSize; windowI++) {
			Arrays.fill(baseCount[windowI], 0);

			for (int baseI = 0; baseI < baseLength; ++baseI) {
				Arrays.fill(qualCount[windowI][baseI], 0);
			}
		}
	}

	public void add(final int windowPosition, final int baseI, final int qual) {
		coverage[windowPosition]++;
		baseCount[windowPosition][baseI]++;
		qualCount[windowPosition][baseI][qual]++;
	}
	
	public int getCoverage(int windowPosition) {
		return coverage[windowPosition];
	}
	
	public int[] getBaseI(int windowPosition) {
		return baseCount[windowPosition];
	}

	public int[][] getQual(int windowPosition) {
		return qualCount[windowPosition];
	}

	public int getWindowSize() {
		return windowsSize;
	}

}