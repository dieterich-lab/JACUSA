package accusa2.pileup.sample;


import java.util.Random;

import accusa2.pileup.Pileup;
import accusa2.pileup.ParallelPileup;
import accusa2.process.phred2prob.Phred2Prob;

public class PermutateParallelPileupQualWithoutReplacement implements PermutateParallelPileup{

	private Random random = new Random(0);  

	public ParallelPileup permutate(ParallelPileup parallelPileup) {
		// containter for permutated parallel pileup
		final ParallelPileup permutated = new ParallelPileup(parallelPileup.getN1(), parallelPileup.getN2());

		Pileup pooled = new Pileup(parallelPileup.getPooledPileup());
		int[] qualCount = collapseQualCount(pooled);

		// hack 
		int[] coverage = {pooled.getCoverage()};

		final Pileup[] permutated1 = permuatePileup(coverage, qualCount, parallelPileup.getPileups1());
		permutated.setPileups1(permutated1);

		final Pileup[] permutated2 = permuatePileup(coverage, qualCount, parallelPileup.getPileups2());
		permutated.setPileups2(permutated2);

		return permutated;
	}

	private Pileup[] permuatePileup(int[] coverage, int[] quals, Pileup[] pileups) {
		Pileup[] permutated = new Pileup[pileups.length];

		for(int j = 0; j < pileups.length; ++j) {
			Pileup pileup = pileups[j];
			permutated[j] = new Pileup();
			// copy base count
			for(int k = 0; k < pileup.getBaseCount().length; ++k) {
				permutated[j].getBaseCount()[k] = pileup.getBaseCount()[k];
			}

			for(int base : permutated[j].getAlleles()) {
				for(int i = 0; i < permutated[j].getBaseCount(base); ++i) {
					byte qual = sampleQual(coverage[0], quals);
					permutated[j].getQualCount()[base][qual]++;
					quals[qual]--;
					coverage[0]--;
				}
			}
		}
		return permutated;
	}
	
	private int[] collapseQualCount(Pileup pileup) {
		int[] quals = new int[Phred2Prob.MAX_Q];
		for(int i = 0; i < Phred2Prob.MAX_Q; ++i) {
			for(int base : pileup.getAlleles()) {
				quals[i] += pileup.getQualCount()[base][i];
			}
		}

		return quals;
	}
	
	private byte sampleQual(final int coverage, final int[] qualCount) {
		final int r = random.nextInt(coverage);
		int count = 0;
		for(int i = 0; i < qualCount.length; ++i) {
			if(r >= count && r < count + qualCount[i]) {
				return (byte)(i);
			}
			count += qualCount[i];
		}

		return -1;
	}

}
