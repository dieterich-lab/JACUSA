package jacusa.estimate;

import java.util.Arrays;

import jacusa.method.call.statistic.dirmult.initalpha.AbstractAlphaInit;

import jacusa.util.Info;
import jacusa.util.MathUtil;

import org.apache.commons.math3.special.Gamma;

// TODO adjust reset
public class MinkaEstimateDirichletParameters extends MinkaEstimateParameters {

	private final static double EPSILON = 0.001;
	private final static int MAX_ITERATIONS = 100;
	
	private int tmpN = Integer.MAX_VALUE;
	private double[] tmpLogProbMean;
	
	public MinkaEstimateDirichletParameters() {
		super();
	}

	public MinkaEstimateDirichletParameters(final AbstractAlphaInit initialAlpha) {
		this(initialAlpha, MAX_ITERATIONS, EPSILON);
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
			final String sample,
			final Info estimateInfo,
			final boolean backtrack) {
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

		int pileupN = pileupMatrix.length;
		
		boolean backtracked = false;
		boolean reseted = false;
		
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
			// try update alphaNew
			boolean admissible = true; 		
			for (int baseI : baseIs) {
				alphaNew[baseI] = alphaOld[baseI] - (gradient[baseI] - b) / Q[baseI];

				if (alphaNew[baseI] < 0.0) {
					// System.err.println("Reset! " + iterations);
					admissible = false;
				}
			}
			// check if alpha negative
			if (! admissible) {
				if (backtracked) {
					estimateInfo.add("backtrack" + sample, ",");
				}
				estimateInfo.add("backtrack" + sample, Integer.toString(iterations));
				backtracked = true;

				// try newton backtracking
				alphaNew = backtracking(alphaOld, baseIs, gradient, b, Q);

				if (alphaNew == null) {
					alphaNew = new double[baseN];

					// if backtracking did not work use Ronning1989 -> min_k X_ik 
					for (int baseI : baseIs) {		
						double min = Double.MAX_VALUE;
						for (int pileupI = 0; pileupI < pileupN; ++pileupI) {
							min = Math.min(min, pileupMatrix[pileupI][baseI]);
						}
						alphaNew[baseI] = min;
					}
					if (reseted) {
						estimateInfo.add("reset" + sample, ",");
					}
					estimateInfo.add("reset" + sample, Integer.toString(iterations));
					reseted = true;
				}
			}

			// calculate log-likelihood for new alpha(s)
			loglikNew = getLogLikelihood(alphaNew, baseIs, pileupMatrix);

			// check if converged
			double delta = Math.abs(loglikNew - loglikOld);
			if (delta  <= EPSILON) {
				converged = true;
			}
			// update value
			System.arraycopy(alphaNew, 0, alphaOld, 0, alphaNew.length);
			iterations++;
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
