package jacusa.method.call.statistic.dirichlet;

import jacusa.cli.parameters.StatisticParameters;

import jacusa.estimate.MinkaEstimateDirichletParameters;
import jacusa.method.call.statistic.AbstractDirichletStatistic;
import jacusa.method.call.statistic.StatisticCalculator;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.Pileup;

import java.util.Arrays;
public class DirichletCompoundError extends AbstractDirichletStatistic {

	private double estimatedError = 0.01;
	protected double priorError = 0d;

	public DirichletCompoundError(final BaseConfig baseConfig, final StatisticParameters parameters) {
		super(new MinkaEstimateDirichletParameters(), baseConfig, parameters);
	}

	@Override
	public StatisticCalculator newInstance() {
		return new DirichletCompoundError(baseConfig, parameters);
	}

	@Override
	public String getName() {
		return "DirCE";
	}

	@Override
	public String getDescription() {
		return "Compound Err. (estimated err.{" + estimatedError + "} + phred score)";
	}

	@Override
	public void populate(final Pileup[] pileups, final int[] baseIs, double[][] pileupMatrix) {
		double[] pileupErrorVector = new double[baseIs.length];
		
		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			Pileup pileup = pileups[pileupI];

			populate(pileup, baseIs, pileupErrorVector, pileupMatrix[pileupI]);
		}
	}

	@Override
	protected void populate(final Pileup pileup, final int[] baseIs, double[] pileupErrorVector, double[] pileupMatrix) {
		Arrays.fill(pileupMatrix, 0.0);

		double[] pileupCount = phred2Prob.colSumCount(baseIs, pileup);
		double[] pileupError = phred2Prob.colMeanErrorProb(baseIs, pileup);

		double sum = 0.0;
		
		for (int baseI : baseIs) {
			pileupMatrix[baseI] += priorError;

			if (pileupCount[baseI] > 0.0) {
				pileupMatrix[baseI] += pileupCount[baseI];
				for (int baseI2 : baseIs) {
					if (baseI != baseI2) {
						double combinedError = (pileupError[baseI2] + estimatedError) * (double)pileupCount[baseI] / (double)(baseIs.length - 1);
						pileupMatrix[baseI2] += combinedError;
						pileupErrorVector[baseI2] = combinedError;
					} else {
						
					}
				}
			} else {
				
			}
			sum += pileupMatrix[baseI];
		}

		for (int baseI : baseIs) {
			pileupMatrix[baseI] /= sum;
		}
	}

}