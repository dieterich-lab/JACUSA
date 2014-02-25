package accusa2.pileup.sample;


import java.util.Random;

import accusa2.pileup.Pileup;
import accusa2.pileup.ParallelPileup;

public class PermutateParallelPileupWithoutReplacement implements PermutateParallelPileup{

	private Random random = new Random(0);  

	public ParallelPileup permutate(ParallelPileup parallelPileup) {
		final ParallelPileup permutated = new ParallelPileup(parallelPileup.getN1(), parallelPileup.getN2());
		
		Pileup pooled = new Pileup(parallelPileup.getPooledPileup());

		final Pileup[] permutated1 = permuatePileup(pooled, parallelPileup.getPileups1());
		permutated.setPileups1(permutated1);

		final Pileup[] permutated2 = permuatePileup(pooled, parallelPileup.getPileups2());
		permutated.setPileups2(permutated2);

		return permutated;
	}

	private Pileup[] permuatePileup(Pileup pooled, Pileup[] pileups) {
		Pileup[] permutated = new Pileup[pileups.length];

		for(int j = 0; j < pileups.length; ++j) {
			Pileup pileup = pileups[j];
			permutated[j] = new Pileup(); 

			for(int i = 0; i < pileup.getCoverage(); ++i) {
				int base = sampleBase(pooled);
				byte qual = sampleQual(base, pooled);

				pooled.removeBase(base, qual);

				permutated[j].addBase(base, qual);
			}
		}
		return permutated;
	}

	private int sampleBase(final Pileup pileup) {
		final int r = random.nextInt(pileup.getCoverage());
		int count = 0;
		for(int base : pileup.getAlleles()) {
			if(r >= count && r < count + pileup.getBaseCount()[base]) {
				return base;
			}
			count += pileup.getBaseCount()[base];
		}

		return -1;
	}

	private byte sampleQual(final int base, final Pileup pileup) {
		final int r = random.nextInt(pileup.getBaseCount(base));
		int count = 0;
		for(int i = 0; i < pileup.getQualCount()[base].length; ++i) {
			if(r >= count && r < count + pileup.getQualCount()[base][i]) {
				return (byte)(i);
			}
			count += pileup.getQualCount()[base][i];
		}

		return -1;
	}

}
