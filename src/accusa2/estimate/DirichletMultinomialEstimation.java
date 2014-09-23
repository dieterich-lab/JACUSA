package accusa2.estimate;

import java.util.Arrays;

import org.apache.commons.math3.special.Gamma;

import accusa2.pileup.Pileup;
import accusa2.process.phred2prob.Phred2Prob;

public class DirichletMultinomialEstimation extends AbstractEstimateParameters {

	// private final double digamma = Math.log(0.5);
	private final int maxIterations = 10000;
	private final double epsilon = 1.0/(double)(10^6); 

	public DirichletMultinomialEstimation(Phred2Prob phred2Prob) {
		super("DirMult", "Dirichlet-Multinomial distribution", phred2Prob);
	}

	@Override
	public double[] estimateAlpha(int[] baseIs, Pileup[] pileups) {
		// parameters
		int iteration = 0;
		boolean converged = false;

		// actual values
		double[] alphaOld = new double[baseIs.length];
		Arrays.fill(alphaOld, 1.0/(double)baseIs.length);
		double[] alphaNew = new double[baseIs.length];
		Arrays.fill(alphaNew, 0.0);
		
		// container 
		double[] gradient = new double[baseIs.length];
		double[] Q = new double[baseIs.length];
		double b;
		double z;
		double summedAlphaOld;
		double digammaSummedAlphaOld;
		double trigammaSummedAlphaOld;
		
		// pileup related counts/containters/with prior knowledge
		double[][] nIK = new double[pileups.length][baseIs.length];
		double[] nI = new double[pileups.length];
		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			nI[pileupI] = (double)pileups[pileupI].getCoverage();
			nIK[pileupI] = phred2Prob.colSum(baseIs, pileups[pileupI]);
		}

		// maximize
		while (iteration < maxIterations && ! converged) {
			// pre-compute
			summedAlphaOld = sum(alphaOld);
			digammaSummedAlphaOld = digamma(summedAlphaOld);
			trigammaSummedAlphaOld = trigamma(summedAlphaOld);
			
			// reset
			b = 0.0;
			double tmp = 0.0;
			for (int baseI = 0; baseI < baseIs.length; ++baseI) {
				// reset
				gradient[baseI] = 0.0;
				Q[baseI] = 0.0;

				for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
					// calculate gradient
					gradient[baseI] += digammaSummedAlphaOld;
					gradient[baseI] -= digamma(nI[pileupI] + summedAlphaOld);
					gradient[baseI] += digamma(nIK[pileupI][baseI] + alphaOld[baseI]);
					gradient[baseI] -= digamma(alphaOld[baseI]);

					// calculate Q
					Q[baseI] += trigamma(nIK[pileupI][baseI] + alphaOld[baseI]);
					Q[baseI] -= trigamma(alphaOld[baseI]);
				}
				
				// calculate b
				b += gradient[baseI] / Q[baseI];
				tmp += 1.0 / Q[baseI];
			}
			
			// calculate z
			z = 0.0;
			for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
				z += trigammaSummedAlphaOld;
				z -= trigamma(nI[pileupI] + summedAlphaOld);
			}
			// calculate b cont.
			b /= (1.0 / z + tmp);
			
			double delta = 0.0;
			// update alphaNew
			for (int baseI = 0; baseI < baseIs.length; ++baseI) {
				alphaNew[baseI] = alphaOld[baseI] - (gradient[baseI] - b) / Q[baseI];
				if (alphaNew[baseI] < 0) {
					alphaNew[baseI] = 0.005;
				}
				delta += Math.abs(alphaNew[baseI] - alphaOld[baseI]);
				alphaOld[baseI] = alphaNew[baseI]; 
			}
			// check if converged
			if (delta <= epsilon) {
				converged = true;
			}
			iteration++;
		}

		return alphaNew;
	}

	@Override
	public double[] estimateExpectedValue(int[] baseIs, Pileup[] pileups) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[][] estimateProbs(int[] baseIs, Pileup[] pileups) {
		// TODO Auto-generated method stub
		return null;
	}

	protected double digamma(double x) {
		return Gamma.digamma(x);
		/*
		if (x >= 0.6) {
			return Math.log(x - 0.5);
		}

		return 1.0 / x - digamma;
		*/
	}

	protected double trigamma(double x) {
		return Gamma.trigamma(x);
	}

	public double sum(double[] values) {
		double sum = 0.0;
		for (double value : values) {
			sum += value;
		}
		return sum;
	}

	// calculate likelihood
	public double getLogLikelihood(double[] alpha, int[] baseIs, Pileup[] pileups) {
		double logLikelihood = 0.0;
		double alphaSum = sum(alpha);

		for (Pileup pileup : pileups) {
			double nI = (double)pileup.getCoverage() ;
			double[] nIK = phred2Prob.colSum(baseIs, pileup);

			logLikelihood += Math.log(Gamma.gamma(alphaSum));
			logLikelihood -= Math.log(Gamma.gamma(nI + alphaSum));
			for (int baseI = 0; baseI < alpha.length; ++baseI) {
				logLikelihood += Math.log(Gamma.gamma(nIK[baseI] - alpha[baseI]));
				logLikelihood -= Math.log(Gamma.gamma(alpha[baseI]));
			}
		}

		return logLikelihood;
	}
	
}