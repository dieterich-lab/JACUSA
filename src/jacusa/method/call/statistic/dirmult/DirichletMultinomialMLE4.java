package jacusa.method.call.statistic.dirmult;

import jacusa.cli.parameters.StatisticParameters;
import jacusa.method.call.statistic.StatisticCalculator;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.Pileup;
import jacusa.util.MathUtil;

import java.util.Arrays;

public class DirichletMultinomialMLE4 extends AbstractDirMult {

	private double estimatedError = 0.01;
	
	public DirichletMultinomialMLE4(final BaseConfig baseConfig, final StatisticParameters parameters) {
		super(baseConfig, parameters);
	}

	@Override
	public String getName() {
		return "DirMult4";
	}

	@Override
	public String getDescription() {
		return "Dirichlet-Multinomial 0.01 Error instead of phred score";
	}

	protected void populate(final Pileup[] pileups, final int[] baseIs, double[] alpha, double[] pileupCoverages, double[][] pileupMatrix) {
		// init
		Arrays.fill(alpha, 0.0);
		Arrays.fill(pileupCoverages, 0.0);
		for (int i = 0; i < pileupMatrix.length; ++i) {
			Arrays.fill(pileupMatrix[i], 0.0);
		}

		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			Pileup pileup = pileups[pileupI];
			double[] pileupCount = phred2Prob.colSumCount(baseIs, pileup);
			pileupMatrix[pileupI] = pileupCount.clone();

			for (int baseI : baseIs) {
				if (pileupCount[baseI] > 0.0) {
					for (int baseI2 : baseIs) {
						if (baseI != baseI2) {
							pileupMatrix[pileupI][baseI2] += estimatedError * (double)pileupCount[baseI] / (double)(baseIs.length - 1);
						}
					}
				}
			}

			pileupCoverages[pileupI] = MathUtil.sum(pileupMatrix[pileupI]);
		}
		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			for (int baseI : baseIs) {
				alpha[baseI] += pileupMatrix[pileupI][baseI];
			}
		}
		for (int baseI : baseIs) {
			alpha[baseI] = alpha[baseI] / (double)pileups.length;
		}
	}

	@Override
	public StatisticCalculator newInstance() {
		return new DirichletMultinomialMLE4(baseConfig, parameters);
	}

}