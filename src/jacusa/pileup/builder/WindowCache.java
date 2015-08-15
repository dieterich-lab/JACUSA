package jacusa.pileup.builder;

import java.util.Arrays;

import jacusa.phred2prob.Phred2Prob;
import jacusa.pileup.Counts;
import jacusa.util.WindowCoordinates;

public class WindowCache {

	private WindowCoordinates windowCoordinates;
	private int baseLength;

	private int[] coverage;
	private int[][] baseCount;
	private int[][][] qualCount;

	private byte[] reference;
	
	private int[][] minQual;
	
	private int[] alleleCount;
	private int[] alleleMask;

	private int windowSize;

	public WindowCache(final WindowCoordinates windowCoordinates, final int baseLength) {
		this.windowCoordinates 	= windowCoordinates;
		this.baseLength 		= baseLength;

		windowSize	= windowCoordinates.getWindowSize();
		coverage = new int[windowSize];
		baseCount = new int[windowSize][baseLength];
		qualCount = new int[windowSize][baseLength][Phred2Prob.MAX_Q];

		reference = new byte[windowSize]; 
		
		minQual	= new int[windowSize][baseLength];;
		
		alleleCount = new int[windowSize];
		alleleMask = new int[windowSize];
	}

	public void clear() {
		Arrays.fill(coverage, 0);
		Arrays.fill(reference, (byte)0);
		for (int windowPositionI = 0; windowPositionI < windowSize; ++windowPositionI) {
			Arrays.fill(baseCount[windowPositionI], 0);
			for (int baseI = 0; baseI < baseLength; ++baseI) {
				Arrays.fill(qualCount[windowPositionI][baseI], 0);
			}
			Arrays.fill(minQual[windowPositionI], 20);
		}

		Arrays.fill(alleleCount, 0);
		Arrays.fill(alleleMask, 0);
	}

	public void addReferenceBase(final int windowPosition, final byte referenceBase) {
		reference[windowPosition] = referenceBase;
	}
	
	public byte getReferenceBase(final int windowPosition) {
		return reference[windowPosition];
	}
	
	public void addHighQualityBaseCall(final int windowPosition, final int baseI, int qualI) {
		// make sure we don't exceed...
		Math.min(Phred2Prob.MAX_Q - 1, qualI);
		++coverage[windowPosition];
		++baseCount[windowPosition][baseI];
		++qualCount[windowPosition][baseI][qualI];

		int r = 2 << baseI;
		int t = alleleMask[windowPosition] & r;
		if (t == 0) {
			alleleMask[windowPosition] += r;
			++alleleCount[windowPosition];
		}
	}

	// only count for alleles
	public void addLowQualityBaseCall(final int windowPosition, final int baseI, final int qualI) {
		int r = 2 << baseI;
		int t = alleleMask[windowPosition] & r;
		if (t == 0) {
			alleleMask[windowPosition] += r;
			++alleleCount[windowPosition];
		}
		minQual[windowPosition][baseI] = Math.min(minQual[windowPosition][baseI], qualI);
	}

	public int getCoverage(final int windowPosition) {
		return coverage[windowPosition];
	}

	public int getAlleleCount(final int windowPosition) {
		return alleleCount[windowPosition];
	}

	public int getAlleleMask(final int windowPosition) {
		return alleleMask[windowPosition];
	}
	
	public int[] getAlleles(int windowPosition) {
		int alleles[] = new int[getAlleleCount(windowPosition)];
		int mask = getAlleleMask(windowPosition);

		int i = 0;
		for (int baseI = 0; baseI < baseLength; ++baseI) {
			if ((mask & 2 << baseI) > 0) {
				alleles[i] = baseI;
				++i;
			}
		}
		
		return alleles;
	}
	
	public int getBaseLength() {
		return baseLength;
	}

	public Counts getCounts(final int windowPosition) {
		return new Counts(baseCount[windowPosition], qualCount[windowPosition], minQual[windowPosition]);
	}
	
	public WindowCoordinates getWindowCoordinates() {
		return windowCoordinates;
	}

	public int getWindowSize() {
		return windowSize;
	}
	
	public byte[] getReference() {
		return reference;
	}
}