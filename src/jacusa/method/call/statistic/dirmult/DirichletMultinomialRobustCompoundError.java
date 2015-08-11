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
				//"Options: epsilon=<epsilon>:maxIterations=<maxIterations>:estimatedError=<estimatedError>";
	}

	@Override
	public double getStatistic(final ParallelPileup parallelPileup) {
		int a1 = parallelPileup.getPooledPileup1().getAlleles().length;
		int a2 = parallelPileup.getPooledPileup2().getAlleles().length;
		int[] alleles = parallelPileup.getPooledPileup().getAlleles();
		int aP = alleles.length;

		int[] variantBaseIs = parallelPileup.getVariantBaseIs();
		int commonBaseI = -1;
		for (int baseI : alleles) {
			int count1 = parallelPileup.getPooledPileup1().getCounts().getBaseCount(baseI);
			int count2 = parallelPileup.getPooledPileup2().getCounts().getBaseCount(baseI);
			if (count1 > 0 && count2  > 0) {
				commonBaseI = baseI;
				break;
			}
		}
		if (variantBaseIs.length == 0) {
			return super.getStatistic(parallelPileup);
		}

		ParallelPileup pp = null;
		if (a1 > 1 && a2 == 1 && aP == 2) {
			pp = new DefaultParallelPileup(parallelPileup.getPileups1(), parallelPileup.getPileups1());
			pp.setPileups1(DefaultPileup.flat(pp.getPileups1(), variantBaseIs, commonBaseI));
		} else if (a2 > 1 && a1 == 1 && aP == 2) {
			pp = new DefaultParallelPileup(parallelPileup.getPileups2(), parallelPileup.getPileups2());
			pp.setPileups2(DefaultPileup.flat(pp.getPileups2(), variantBaseIs, commonBaseI));
		}
		if (pp == null) {
			return super.getStatistic(parallelPileup);
		}
		
		return super.getStatistic(pp);
	}
	
}