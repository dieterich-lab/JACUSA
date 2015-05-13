package jacusa.pileup.sample;

import jacusa.pileup.DefaultParallelPileup;
import jacusa.pileup.DefaultPileup;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Pileup;

import java.util.Random;

@Deprecated
public class PermutateBases implements PermutateParallelPileup {

	private Random random = new Random(0);  

	public ParallelPileup permutate(ParallelPileup parallelPileup) {
		// containter for permutated parallel pileup
		final ParallelPileup permutated = new DefaultParallelPileup(parallelPileup.getN1(), parallelPileup.getN2());

		// 
		Pileup pooled = new DefaultPileup(parallelPileup.getPooledPileup());
		
		int[] coverages1 = new int[parallelPileup.getN1()];
		for (int i = 0; i < parallelPileup.getN1(); ++i) {
			coverages1[i] = parallelPileup.getPileups1()[i].getCoverage();
		}
		int[] coverages2 = new int[parallelPileup.getN2()];
		for (int i = 0; i < parallelPileup.getN2(); ++i) {
			coverages2[i] = parallelPileup.getPileups2()[i].getCoverage();
		}
		
		final DefaultPileup[] permutated1 = permuatePileup(coverages1, pooled);
		permutated.setPileups1(permutated1);

		final DefaultPileup[] permutated2 = permuatePileup(coverages2, pooled);
		permutated.setPileups2(permutated2);

		return permutated;
	}

	private DefaultPileup[] permuatePileup(int[] coverages, Pileup pooled) {
		DefaultPileup[] permutated = new DefaultPileup[coverages.length];

		for (int j = 0; j < permutated.length; ++j) {
			// Pileup pileup = new DefaultPileup(pooled.getCounts().getBaseCount().length);
			permutated[j] = new DefaultPileup(pooled.getCounts().getBaseLength());

			for (int i = 0; i < coverages[j]; ++i) {
				int baseI = sampleBaseI(pooled);
				byte qual = sampleQual(baseI, pooled);
				permutated[j].getCounts().addBase(baseI, qual);
				pooled.getCounts().removeBase(baseI, qual);
			}
		}

		return permutated;
	}

	private int sampleBaseI(Pileup pooled) {
		final int r = random.nextInt(pooled.getCoverage());
		int count = 0;

		for (int baseI : pooled.getAlleles()) {
			if (r >= count && r < count + pooled.getCounts().getBaseCount(baseI)) {
				return baseI;
			}
			count += pooled.getCounts().getBaseCount(baseI);
		}

		return -1;
	}

	private byte sampleQual(int baseI, Pileup pooled) {
		final int r = random.nextInt(pooled.getCounts().getBaseCount(baseI));

		int[] qualCount = pooled.getCounts().getQualCount(baseI);
		int count = 0;
		for (int i = 0; i < qualCount.length; ++i) {
			if (r >= count && r < count + qualCount[i]) {
				return (byte)(i);
			}
			count += qualCount[i];
		}

		return -1;
	}

}