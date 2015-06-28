package jacusa.pileup;

import java.util.Arrays;

import jacusa.phred2prob.Phred2Prob;

public class Counts implements Cloneable {

	// container
	private int[] baseCount;
	private int[][] base2qual;
	private int[] minQual;

	public Counts(final int baseLength) {
		baseCount 	= new int[baseLength];
		base2qual	= new int[baseLength][Phred2Prob.MAX_Q];
		minQual		= new int[baseLength];
		Arrays.fill(minQual, Phred2Prob.MAX_Q);
	}

	public Counts(final int[] baseCount, final int[][] base2qual, int[] minMapq) {
		this(baseCount.length);
		
		System.arraycopy(baseCount, 0, this.baseCount, 0, baseCount.length);
		for (int baseI = 0; baseI < baseCount.length; ++baseI) {
			if (baseCount[baseI] > 0) {
				System.arraycopy(base2qual[baseI], 0, this.base2qual[baseI], 0, base2qual[baseI].length);
			}
		}
		System.arraycopy(minMapq, 0, this.minQual, 0, minMapq.length);
	}
	
	public Counts(Counts counts) {
		this(counts.baseCount.length);
		
		System.arraycopy(counts.baseCount, 0, this.baseCount, 0, counts.baseCount.length);
		for (int baseI = 0; baseI < counts.baseCount.length; ++baseI) {
			if (counts.baseCount[baseI] > 0) {
				System.arraycopy(counts.base2qual[baseI], 0, base2qual[baseI], 0, counts.base2qual[baseI].length);
			}
		}
		System.arraycopy(counts.minQual,0, minQual, 0, counts.minQual.length);
	}

	/*
	public void reset() {
		int baseLength = baseCount != null && baseCount.length > 0 ? baseCount.length : 0;
		Arrays.fill(baseCount, 0);
		for (int baseI = 0; baseI < baseLength; ++baseI) {
			Arrays.fill(base2qual[baseI], 0);
		}
		Arrays.fill(minQual, Phred2Prob.MAX_Q);
	}
	*/

	public void addBase(final int baseI, final int qualI) {
		++baseCount[baseI];
		++base2qual[baseI][qualI];
		minQual[baseI] = Math.min(qualI, minQual[baseI]);
	}

	public void removeBase(final int base, final int qualI) {
		--baseCount[base];
		--base2qual[base][qualI];
	}

	public int getCoverage() {
		int coverage = 0;
		
		for (int c : baseCount) {
			coverage += c;
		}

		return coverage;
	}

	public int getQualCount(final int base, final int qualI) {
		return base2qual[base][qualI];
	}
	
	public int[] getQualCount(final int base) {
		return base2qual[base];
	}
	
	public int getBaseCount(final int base) {
		return baseCount[base];
	}

	public void addCounts(final Counts counts) {
		for (int baseI = 0; baseI < counts.baseCount.length; ++baseI) {
			if (counts.baseCount[baseI] > 0) {
				add(baseI, counts);
			}
		}
	}

	public void add(final int baseI, final Counts counts) {
		baseCount[baseI] += counts.baseCount[baseI];

		for (int qualI = counts.minQual[baseI]; qualI < Phred2Prob.MAX_Q ; ++qualI) {
			if (counts.base2qual[baseI][qualI] > 0) {
				base2qual[baseI][qualI] += counts.base2qual[baseI][qualI];
			}
		}
	}

	public void add(final int baseI, final int baseI2, final Counts counts) {
		baseCount[baseI] += counts.baseCount[baseI2];

		for (int qualI = counts.minQual[baseI2]; qualI < Phred2Prob.MAX_Q ; ++qualI) {
			if (counts.base2qual[baseI2][qualI] > 0) {
				base2qual[baseI][qualI] += counts.base2qual[baseI2][qualI];
			}
		}
	}
	
	public void substract(final int baseI, final Counts counts) {
		baseCount[baseI] -= counts.baseCount[baseI];

		for (int qualI = counts.minQual[baseI]; qualI < Phred2Prob.MAX_Q ; ++qualI) {
			if (counts.base2qual[baseI][qualI] > 0) {
				base2qual[baseI][qualI] -= counts.base2qual[baseI][qualI];
			}
		}
	}

	public void substract(final int baseI, final int baseI2, final Counts counts) {
		baseCount[baseI] -= counts.baseCount[baseI2];

		for (int qualI = counts.minQual[baseI2]; qualI < Phred2Prob.MAX_Q ; ++qualI) {
			if (counts.base2qual[baseI2][qualI] > 0) {
				base2qual[baseI][qualI] -= counts.base2qual[baseI2][qualI];
			}
		}
	}
	
	public void substract(final Counts counts) {
		for (int baseI = 0; baseI < counts.baseCount.length; ++baseI) {
			if (baseCount[baseI] > 0) {
				substract(baseI, counts);
			}
		}
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
			tmpQualCount[complementaryBaseI] = base2qual[baseI];
		}

		baseCount = tmpBaseCount;
		base2qual = tmpQualCount;
	}

	@Override
	public Object clone() {
		return new Counts(this);
	}

	public int getBaseLength() {
		return baseCount.length;
	}
	
}