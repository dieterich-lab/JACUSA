package accusa2.pileup.sample;


import java.util.Random;

import accusa2.pileup.DefaultPileup;
import accusa2.pileup.DefaultParallelPileup;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;

public class PermutateParallelPileupWithoutReplacement implements PermutateParallelPileup{

	private Random random = new Random(0);  

	public ParallelPileup permutate(ParallelPileup parallelPileup) {
		final ParallelPileup permutated = new DefaultParallelPileup(parallelPileup.getNA(), parallelPileup.getNB());
		
		Pileup pooled = new DefaultPileup(parallelPileup.getPooledPileup());

		final DefaultPileup[] permutated1 = permuatePileup(pooled, parallelPileup.getPileupsA());
		permutated.setPileupsA(permutated1);

		final DefaultPileup[] permutated2 = permuatePileup(pooled, parallelPileup.getPileupsB());
		permutated.setPileupsB(permutated2);

		return permutated;
	}

	private DefaultPileup[] permuatePileup(Pileup pooled, Pileup[] pileups) {
		DefaultPileup[] permutated = new DefaultPileup[pileups.length];

		for(int j = 0; j < pileups.length; ++j) {
			Pileup pileup = pileups[j];
			permutated[j] = new DefaultPileup(); 

			for(int i = 0; i < pileup.getCoverage(); ++i) {
				int base = sampleBase(pooled);
				byte qual = sampleQual(base, pooled);

				pooled.getCounts().removeBase(base, qual);

				permutated[j].getCounts().addBase(base, qual);
			}
		}
		return permutated;
	}

	private int sampleBase(final Pileup pileup) {
		final int r = random.nextInt(pileup.getCoverage());
		int count = 0;
		for(int base : pileup.getAlleles()) {
			if(r >= count && r < count + pileup.getCounts().getBaseCount()[base]) {
				return base;
			}
			count += pileup.getCounts().getBaseCount()[base];
		}

		return -1;
	}

	private byte sampleQual(final int base, final Pileup pileup) {
		final int r = random.nextInt(pileup.getCounts().getBaseCount(base));
		int count = 0;
		for(int i = 0; i < pileup.getCounts().getQualCount()[base].length; ++i) {
			if(r >= count && r < count + pileup.getCounts().getQualCount()[base][i]) {
				return (byte)(i);
			}
			count += pileup.getCounts().getQualCount()[base][i];
		}

		return -1;
	}

}
