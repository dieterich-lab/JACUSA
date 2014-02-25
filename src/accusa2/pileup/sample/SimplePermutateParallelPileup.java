package accusa2.pileup.sample;

import java.util.Random;

import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;


public class SimplePermutateParallelPileup {

	private Random random = new Random(0);  

	public ParallelPileup permutate(ParallelPileup parallelPileup) {
		final int count = parallelPileup.getN1() + parallelPileup.getN2(); 
		final Pileup[] pileups = new Pileup[count];
		System.arraycopy(parallelPileup.getPileups1(), 0, pileups, 0, parallelPileup.getN1());
		System.arraycopy(parallelPileup.getPileups2(), 0, pileups, parallelPileup.getN1(), parallelPileup.getN2());

		for(int i = 0; i < count; ++i) {
			final int r = random.nextInt(count);
			final Pileup tmp = pileups[i];
			pileups[i] = pileups[r];
			pileups[r] = tmp;
		}
		
		final ParallelPileup permutated = new ParallelPileup(parallelPileup.getN1(), parallelPileup.getN2());
		final Pileup[] pileups1 = new Pileup[parallelPileup.getN1()];
		permutated.setPileups1(pileups1);
		final Pileup[] pileups2 = new Pileup[parallelPileup.getN2()];
		permutated.setPileups2(pileups2);

		return permutated;
	}

}
