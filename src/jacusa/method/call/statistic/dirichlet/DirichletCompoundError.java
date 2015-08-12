package jacusa.method.call.statistic.dirichlet;

import jacusa.cli.parameters.StatisticParameters;

import jacusa.estimate.MinkaEstimateDirichletParameters;
import jacusa.method.call.statistic.AbstractDirichletStatistic;
import jacusa.method.call.statistic.StatisticCalculator;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.Pileup;

import java.util.Arrays;

/**
 * 
 * @author Michael Piechotta
 */
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
		return "Dir-CE";
	}

	@Override
	public String getDescription() {
		return "Compound Err. (estimated err.{" + estimatedError + "} + phred score)";
	}

	@Override
	protected void populate(final Pileup pileup, final int[] baseIs, double[] pileupErrorVector, double[] pileupMatrix) {
		// init pileup Matrix
		Arrays.fill(pileupMatrix, 0.0);

		// get total base counts
		double[] pileupCount = phred2Prob.colSumCount(baseIs, pileup);
		// get mean error probs. per base
		double[] pileupError = phred2Prob.colMeanErrorProb(baseIs, pileup);

		double total = 0.0;

		for (int baseI : baseIs) {
			// add pseudo count (prior information) 
			pileupMatrix[baseI] += priorError;

			if (pileupCount[baseI] > 0.0) {
				pileupMatrix[baseI] += pileupCount[baseI];
				
				for (int baseI2 : baseIs) {
					// distribute errors / pseudocounts on uncalled bases 
					if (baseI != baseI2) {
						double combinedError = (pileupError[baseI2] + estimatedError) * 
								(double)pileupCount[baseI] / (double)(baseIs.length - 1);
						pileupMatrix[baseI2] += combinedError;
						// keep track of total pseudocount per base
						pileupErrorVector[baseI2] = combinedError;
					} else {
						// "correct" base call
						// nothing to be done, yet
					}
				}
			} else {
				// base not observed
				// nothing to be done, yet
			}
			total += pileupMatrix[baseI];
		}

		// normalize giving probability matrix
		for (int baseI : baseIs) {
			pileupMatrix[baseI] /= total;
		}
	}

}