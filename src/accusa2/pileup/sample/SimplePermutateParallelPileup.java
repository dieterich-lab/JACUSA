package accusa2.pileup.sample;

import java.util.Random;

import accusa2.pileup.DefaultParallelPileup;
import accusa2.pileup.DefaultPileup;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;


public class SimplePermutateParallelPileup {

	private Random random = new Random(0);  

	public ParallelPileup permutate(ParallelPileup parallelPileup) {
		final int count = parallelPileup.getNA() + parallelPileup.getNB(); 
		final Pileup[] pileups = new Pileup[count];
		System.arraycopy(parallelPileup.getPileupsA(), 0, pileups, 0, parallelPileup.getNA());
		System.arraycopy(parallelPileup.getPileupsB(), 0, pileups, parallelPileup.getNA(), parallelPileup.getNB());

		for(int i = 0; i < count; ++i) {
			final int r = random.nextInt(count);
			final Pileup tmp = pileups[i];
			pileups[i] = pileups[r];
			pileups[r] = tmp;
		}
		
		final ParallelPileup permutated = new DefaultParallelPileup(parallelPileup.getNA(), parallelPileup.getNB());
		final DefaultPileup[] pileups1 = new DefaultPileup[parallelPileup.getNA()];
		permutated.setPileupsA(pileups1);
		final DefaultPileup[] pileups2 = new DefaultPileup[parallelPileup.getNB()];
		permutated.setPileupsB(pileups2);

		return permutated;
	}

}
