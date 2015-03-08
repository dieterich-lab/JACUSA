package jacusa.method.call.statistic.dirmult;

import jacusa.cli.parameters.StatisticParameters;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.DefaultParallelPileup;
import jacusa.pileup.DefaultPileup;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Pileup;

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
		return "Dirichlet-Multinomial-Robust with compound error: (estimated error + phred score) ; estimated error = " + estimatedError + 
				" (DirMult-CE:epsilon=<epsilon>:maxIterations=<maxIterations>:estimatedError=<estimatedError>)";
	}

	@Override
	public double getStatistic(ParallelPileup parallelPileup) {
		int a1 = parallelPileup.getPooledPileup1().getAlleles().length;
		int a2 = parallelPileup.getPooledPileup2().getAlleles().length;
		int[] alleles = parallelPileup.getPooledPileup().getAlleles();
		int aP = alleles.length;

		int[] variantBaseIs = parallelPileup.getVariantBaseIs();
		int targetBaseI = -1;
		for (int baseI : alleles) {
			int count1 = parallelPileup.getPooledPileup1().getCounts().getBaseCount(baseI);
			int count2 = parallelPileup.getPooledPileup2().getCounts().getBaseCount(baseI);
			if (count1 > 0 && count2  > 0) {
				targetBaseI = baseI;
				break;
			}
		}
		if (variantBaseIs.length == 0) {
			/*
			double unpooled = super.getStatistic(parallelPileup);
			double pooled = super.getStatistic(DefaultParallelPileup.Pool(parallelPileup));
			return Math.max(unpooled, pooled);
			*/
			return super.getStatistic(parallelPileup);
		}

		ParallelPileup pp = null;
		if (a1 > 1 && a2 == 1 && aP == 2) {
			pp = new DefaultParallelPileup(parallelPileup.getPileups1(), parallelPileup.getPileups1());
			pp.setPileups1(flat(pp.getPileups1(), variantBaseIs, targetBaseI));
			
		} else if (a2 > 1 && a1 == 1 && aP == 2) {
			pp = new DefaultParallelPileup(parallelPileup.getPileups2(), parallelPileup.getPileups2());
			pp.setPileups2(flat(pp.getPileups2(), variantBaseIs, targetBaseI));
		}
		if (pp == null) {
			/*
			double unpooled = super.getStatistic(parallelPileup);
			double pooled = super.getStatistic(DefaultParallelPileup.Pool(parallelPileup));
			return Math.max(unpooled, pooled);
			*/
			return super.getStatistic(parallelPileup);
		}

		/*
		double unpooled = super.getStatistic(pp);
		double pooled = super.getStatistic(DefaultParallelPileup.Pool(pp));
		return Math.max(unpooled, pooled);
		*/
		return super.getStatistic(pp);
	}

	private Pileup[] flat(Pileup[] pileups, int[] variantBaseIs, int baseI) {
		Pileup[] ret = new Pileup[pileups.length];
		for (int i = 0; i < pileups.length; ++i) {
			ret[i] = new DefaultPileup(pileups[i]);
			
			for (int variantI : variantBaseIs) {
				// base
				ret[i].getCounts().getBaseCount()[baseI] += ret[i].getCounts().getBaseCount()[variantI];
				ret[i].getCounts().getBaseCount()[variantI] = 0;
				
				// qual
				for (int qualI = ret[i].getCounts().getMinQualI(); qualI < ret[i].getCounts().getQualCount()[variantI].length; ++qualI) {
					ret[i].getCounts().getQualCount()[baseI][qualI] += ret[i].getCounts().getQualCount()[variantI][qualI];
					ret[i].getCounts().getQualCount()[variantI][qualI] = 0;
				}
			}
		}
		return ret;
	}
	
}