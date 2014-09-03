package accusa2.pileup.sample;


import java.util.Random;

import accusa2.pileup.DefaultPileup;
import accusa2.pileup.DefaultParallelPileup;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;
import accusa2.process.phred2prob.Phred2Prob;

public class PermutateParallelPileupQualWithoutReplacement implements PermutateParallelPileup{

	private Random random = new Random(0);  

	public ParallelPileup permutate(ParallelPileup parallelPileup) {
		// containter for permutated parallel pileup
		final ParallelPileup permutated = new DefaultParallelPileup(parallelPileup.getNA(), parallelPileup.getNB());

		Pileup pooled = new DefaultPileup(parallelPileup.getPooledPileup());
		int[] qualCount = collapseQualCount(pooled);

		// hack 
		int[] coverage = {pooled.getCoverage()};

		final DefaultPileup[] permutated1 = permuatePileup(coverage, qualCount, parallelPileup.getPileupsA());
		permutated.setPileupsA(permutated1);

		final DefaultPileup[] permutated2 = permuatePileup(coverage, qualCount, parallelPileup.getPileupsB());
		permutated.setPileupsB(permutated2);

		return permutated;
	}

	private DefaultPileup[] permuatePileup(int[] coverage, int[] quals, Pileup[] pileups) {
		DefaultPileup[] permutated = new DefaultPileup[pileups.length];

		for(int j = 0; j < pileups.length; ++j) {
			Pileup pileup = pileups[j];
			permutated[j] = new DefaultPileup(pileup.getBaseCount().length);
			// copy base count
			for(int k = 0; k < pileup.getCounts().getBaseCount().length; ++k) {
				permutated[j].getCounts().getBaseCount()[k] = pileup.getCounts().getBaseCount()[k];
			}

			for(int base : permutated[j].getAlleles()) {
				for(int i = 0; i < permutated[j].getCounts().getBaseCount(base); ++i) {
					byte qual = sampleQual(coverage[0], quals);
					permutated[j].getCounts().getQualCount()[base][qual]++;
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
				quals[i] += pileup.getCounts().getQualCount()[base][i];
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
