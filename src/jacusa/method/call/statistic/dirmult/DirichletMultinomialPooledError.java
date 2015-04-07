package jacusa.method.call.statistic.dirmult;

import jacusa.cli.parameters.StatisticParameters;
import jacusa.method.call.statistic.StatisticCalculator;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.Pileup;
import jacusa.util.MathUtil;

import java.util.Arrays;

public class DirichletMultinomialPooledError extends AbstractDirMultStatistic {

	public DirichletMultinomialPooledError(final BaseConfig baseConfig, final StatisticParameters parameters) {
		super(baseConfig, parameters);
	}

	@Override
	public String getName() {
		return "DirMult-PE";
	}

	@Override
	public String getDescription() {
		return "Dirichlet-Multinomial - PooledError (DirMult-PE:epsilon=<epsilon>:maxIterations=<maxIterations>)";
	}

	@Override
	protected void populate(Pileup[] pileups, int[] baseIs, double[] pileupCoverages, double[][] pileupMatrix) {
		// init
		Arrays.fill(pileupCoverages, 0.0);
		for (int i = 0; i < pileupMatrix.length; ++i) {
			Arrays.fill(pileupMatrix[i], 0.0);
		}

		double[] pooledError = phred2Prob.pooledPileupErrorProb(baseIs, pileups); 

		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			Pileup pileup = pileups[pileupI];
			pileupMatrix[pileupI] = phred2Prob.colSumCount(baseIs, pileup);

			for (int baseI : baseIs) {
				if (pooledError[baseI] > 0.0) {
					pileupMatrix[pileupI][baseI] += pooledError[baseI] / (double)pileups.length * (double)pileup.getCoverage();
				}
			}

			pileupCoverages[pileupI] = MathUtil.sum(pileupMatrix[pileupI]);
		}
	}
	
	@Override
	public StatisticCalculator newInstance() {
		return new DirichletMultinomialPooledError(baseConfig, parameters);
	}

}