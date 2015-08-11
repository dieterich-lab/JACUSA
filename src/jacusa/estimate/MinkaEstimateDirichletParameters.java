package jacusa.estimate;

import java.util.Arrays;

import jacusa.method.call.statistic.dirmult.initalpha.AbstractAlphaInit;

import jacusa.util.MathUtil;

import org.apache.commons.math3.special.Gamma;

public class MinkaEstimateDirichletParameters extends MinkaEstimateParameters {

	private int tmpN = Integer.MAX_VALUE;
	private double[] tmpLogProbMean;
	
	
	public MinkaEstimateDirichletParameters() {
		super();
	}

	public MinkaEstimateDirichletParameters(
			final AbstractAlphaInit initialAlpha, 
			final int maxIterations, 
			final double epsilon) {
		super(initialAlpha, maxIterations, epsilon);
	}
	
	public double maximizeLogLikelihood(
			final int[] baseIs, 
			final double[] alphaOld, 
			final double[][] pileupMatrix, 
			final StringBuilder info) {
		// parameters
		int iteration = 0;
		boolean converged = false;

		final double[] localLogProbMean = getLogProbMean(baseIs, pileupMatrix);
		final int localN = getN(pileupMatrix);

		int baseN = alphaOld.length;
		
		double[] alphaNew = new double[baseN];
		Arrays.fill(alphaNew, 0.0);

		// container 
		double[] gradient = new double[baseN];
		double[] Q = new double[baseN];
		double b;
		double z;
		double summedAlphaOld;
		double digammaSummedAlphaOld;
		double trigammaSummedAlphaOld;
		double loglikOld = Double.NEGATIVE_INFINITY;;
		double loglikNew = Double.NEGATIVE_INFINITY;

		// maximize
		while (iteration < maxIterations && ! converged) {
			// pre-compute
			summedAlphaOld = MathUtil.sum(alphaOld);
			digammaSummedAlphaOld = digamma(summedAlphaOld);
			trigammaSummedAlphaOld = trigamma(summedAlphaOld);

			// reset
			b = 0.0;
			double b_DenominatorSum = 0.0;
			for (int baseI : baseIs) {
				// calculate gradient
				// reset
				gradient[baseI] = (double)localN * digammaSummedAlphaOld;
				gradient[baseI] -= (double)localN * digamma(alphaOld[baseI]);
				gradient[baseI] += (double)localN * localLogProbMean[baseI];

				// calculate Q
				// reset
				Q[baseI] = -(double)localN * trigamma(alphaOld[baseI]);

				// calculate b
				b += gradient[baseI] / Q[baseI];
				b_DenominatorSum += 1.0 / Q[baseI];
			}

			// calculate z
			z = (double)localN * trigammaSummedAlphaOld;
			// calculate b cont.
			b = b / (1.0 / z + b_DenominatorSum);

			loglikOld = getLogLikelihood(alphaOld, baseIs, pileupMatrix);
			// update alphaNew
			for (int baseI : baseIs) {
				alphaNew[baseI] = alphaOld[baseI] - (gradient[baseI] - b) / Q[baseI];
				// check that alpha is not < 0
				if (alphaNew[baseI] < 0) {
					alphaNew[baseI] = 0.005; // hard set
				}
			}
			loglikNew = getLogLikelihood(alphaNew, baseIs, pileupMatrix);

			// check if converged
			double delta = Math.abs(loglikNew - loglikOld);
			if (delta  <= epsilon) {
				converged = true;
			}
			// update value
			System.arraycopy(alphaNew, 0, alphaOld, 0, baseN);

			iteration++;
		}

		// reset
		tmpN = Integer.MAX_VALUE;
		tmpLogProbMean = null;
		
		return loglikNew;
	}
	
	private int getN(final double[][] pileupMatrix) {
		if (tmpN == Integer.MAX_VALUE) {
			tmpN = pileupMatrix.length;
		}
		
		return tmpN;
	}
	
	private double[] getLogProbMean(final int[] baseIs, final double[][] pileupMatrix) {
		if (tmpLogProbMean == null) {
			
		}
		
		return tmpLogProbMean;
	}
	
	// calculate likelihood
	public double getLogLikelihood(double[] alpha, int[] baseIs, double[][] pileupMatrix) {
		final double alphaSum = MathUtil.sum(alpha);
		final double[] logProbMean = getLogProbMean(baseIs, pileupMatrix);
		final int N = getN(pileupMatrix);
		
		double logLikelihood = (double)N * Gamma.logGamma(alphaSum);
		double tmp = 0.0;
		double tmp2 = 0.0;
		for (int baseI : baseIs) {
			tmp += Gamma.logGamma(alpha[baseI]);
			tmp2 += (alpha[baseI] - 1.0) * logProbMean[baseI];
		}
		logLikelihood -= (double)N * tmp;
		logLikelihood += (double)N * tmp2;

		return logLikelihood;
	}

}
