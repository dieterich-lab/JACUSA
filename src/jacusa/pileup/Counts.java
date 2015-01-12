package jacusa.pileup;

import jacusa.process.phred2prob.Phred2Prob;

public class Counts implements Cloneable {

	// container
	private int[] baseCount;
	private int[][] qualCount;

	private int minQualI;

	public Counts(final int baseLength, final int minQualI) {
		baseCount 		= new int[baseLength];
		qualCount		= new int[baseLength][Phred2Prob.MAX_Q];
		
		this.minQualI = minQualI;
	}

	public Counts(Counts count) {
		this(count.baseCount.length, count.minQualI);
		System.arraycopy(count.baseCount, 0, this.baseCount, 0, count.baseCount.length);

		for (int baseI = 0; baseI < count.baseCount.length; ++baseI) {
			System.arraycopy(count.qualCount[baseI], 0, qualCount[baseI], 0, count.qualCount[baseI].length);
		}
	}
	
	public Counts(final int[] baseCount, final int[][] qualCount) {
		this.baseCount = new int[baseCount.length];
		System.arraycopy(baseCount, 0, this.baseCount, 0, baseCount.length);

		this.qualCount = new int[baseCount.length][qualCount[0].length];
		for(int baseI = 0; baseI < baseCount.length; ++baseI) {
			System.arraycopy(qualCount[baseI], 0, this.qualCount[baseI], 0, qualCount[baseI].length);
		}
	}

	public void addBase(final int baseI, final byte qualI) {
		++baseCount[baseI];
		++qualCount[baseI][qualI];
	}

	public void removeBase(final int base, final byte qual) {
		--baseCount[base];
		--qualCount[base][qual];
	}

	public int getQualCount(final int base, final byte qual) {
		return qualCount[base][qual];
	}
	
	public int getBaseCount(final int base) {
		return baseCount[base];
	}

	public void addCounts(final Counts counts) {
		for (int baseI = 0; baseI < counts.baseCount.length; ++baseI) {
			baseCount[baseI] += counts.baseCount[baseI];
			
			for (int qualI = minQualI; qualI < counts.qualCount[baseI].length; ++qualI) {
				qualCount[baseI][qualI] += counts.qualCount[baseI][qualI];
			}
		}
	}
	
	public void substract(final int baseI, final Counts counts) {
		baseCount[baseI] -= counts.baseCount[baseI];

		for (int qualI = minQualI; qualI < counts.qualCount[baseI].length; ++qualI) {
			qualCount[baseI][qualI] -= counts.qualCount[baseI][qualI];
		}
	}

	public void substract(final Counts counts) {
		for (int baseI = 0; baseI < counts.baseCount.length; ++baseI) {
			baseCount[baseI] -= counts.baseCount[baseI];
			
			for (int qualI = minQualI; qualI < counts.qualCount[baseI].length; ++qualI) {
				qualCount[baseI][qualI] -= counts.qualCount[baseI][qualI];
			}
		}
	}

	public int[] getBaseCount() {
		return baseCount;
	}
	
	public int[][] getQualCount() {
		return qualCount;
	}

	public void setBaseCount(int[] baseCount) {
		this.baseCount = baseCount;
	}

	public void setQualCount(int [][] qualCount) {
		this.qualCount = qualCount;
	}

	public void invertCounts() {
		int[] tmpBaseCount = new int[baseCount.length];
		int[][] tmpQualCount = new int[baseCount.length][Phred2Prob.MAX_Q];

		for (int baseI = 0; baseI < baseCount.length; ++baseI) {
			// int complementaryBase = Bases.COMPLEMENT[base];
			int complementaryBaseI = baseCount.length - baseI - 1;  

			// invert base count
			tmpBaseCount[complementaryBaseI] = baseCount[baseI];
			// invert qualCount
			tmpQualCount[complementaryBaseI] = qualCount[baseI];
		}

		baseCount = tmpBaseCount;
		qualCount = tmpQualCount;
	}

	public int getMinQualI() {
		return minQualI;
	}
	
	@Override
	public Object clone() {
		return new Counts(this);
	}

}