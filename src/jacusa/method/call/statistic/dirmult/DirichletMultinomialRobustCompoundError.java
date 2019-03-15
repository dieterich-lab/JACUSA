package jacusa.method.call.statistic.dirmult;

import jacusa.cli.parameters.StatisticParameters;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.DefaultParallelPileup;
import jacusa.pileup.DefaultPileup;
import jacusa.pileup.ParallelPileup;

public class DirichletMultinomialRobustCompoundError extends DirichletMultinomialCompoundError {

	public DirichletMultinomialRobustCompoundError(final BaseConfig baseConfig, final StatisticParameters parameters) {
		super(baseConfig, parameters);
	}

	@Override
	public String getName() {
		return "DirMult-RCE";
	}

	@Override
	public String getDescription() {
		return "Robust Compound Err.";  
	}

	@Override
	public double getStatistic(final ParallelPileup parallelPileup) {
		/* 
		 * check if any sample is homomorph and
		 * replace this sample with the other sample and make it homomorph 
		 */
		
		// determine the number of alleles per sample: 1, 2, and P 
		int a1 = parallelPileup.getPooledPileup1().getAlleles().length;
		int a2 = parallelPileup.getPooledPileup2().getAlleles().length;
		// all observed alleles
		int[] alleles = parallelPileup.getPooledPileup().getAlleles();
		int aP = alleles.length;

		// get bases that are different between the samples
		int[] variantBaseIs = parallelPileup.getVariantBaseIs();
		// if there are no variant bases than both samples are heteromorph; 
		// use existing parallelPileup to calculate test-statistic
		if (variantBaseIs.length == 0) {
			return super.getStatistic(parallelPileup);
		}

		// determine common base (shared by both samples)
		final char refBase = parallelPileup.getPooledPileup().getRefBase();
		final int refBaseI = baseConfig.getBaseI((byte)refBase);

		// container for adjusted parallelPileup
		ParallelPileup adjustedParallelPileup = null;
		// determine which sample has the variant base
		if (a1 > 1 && a2 == 1 && aP == 2) { // sample1
			// create new container that fits 2 x pileups1
			adjustedParallelPileup = new DefaultParallelPileup(parallelPileup.getPileups1(), parallelPileup.getPileups1());
			// and replace pileups2 with pileups1 where the variant bases have been replaced with the common base
			adjustedParallelPileup.setPileups2(DefaultPileup.flat(adjustedParallelPileup.getPileups1(), variantBaseIs, refBaseI));
		} else if (a2 > 1 && a1 == 1 && aP == 2) { // sample2
			// do the same for sample 2
			adjustedParallelPileup = new DefaultParallelPileup(parallelPileup.getPileups2(), parallelPileup.getPileups2());
			adjustedParallelPileup.setPileups1(DefaultPileup.flat(adjustedParallelPileup.getPileups2(), variantBaseIs, refBaseI));
		}
		// aP > 3, just use the existing parallelPileup to calculate the test-statistic
		if (adjustedParallelPileup == null) { 
			return super.getStatistic(parallelPileup);
		}
		
		return super.getStatistic(adjustedParallelPileup);
	}

}