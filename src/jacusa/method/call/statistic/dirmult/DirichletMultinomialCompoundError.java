package jacusa.method.call.statistic.dirmult;

import jacusa.cli.parameters.StatisticParameters;
import jacusa.filter.factory.AbstractFilterFactory;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.Pileup;
import jacusa.util.MathUtil;

import java.util.Arrays;


public class DirichletMultinomialCompoundError extends AbstractDirMultStatistic {

	private double estimatedError = 0.01;

	public DirichletMultinomialCompoundError(final BaseConfig baseConfig, final StatisticParameters parameters) {
		super(baseConfig, parameters);
	}

	@Override
	public String getName() {
		return "DirMult-CE";
	}

	@Override
	public String getDescription() {
		return "Dirichlet-Multinomial with compound error: (estimated error + phred score) ; estimated error = " + estimatedError + " (DirMult-CE:epsilon:maxIterations:estimatedError)";
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
			double[] pileupError = phred2Prob.colMeanErrorProb(baseIs, pileup);

			for (int baseI : baseIs) {
				pileupMatrix[pileupI][baseI] += pileupCount[baseI];
				if (pileupCount[baseI] > 0.0) {
					for (int baseI2 : baseIs) {
						if (baseI != baseI2) {
							pileupMatrix[pileupI][baseI2] += (pileupError[baseI2] + estimatedError) * (double)pileupCount[baseI] / (double)(baseIs.length - 1);
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
	public DirichletMultinomialCompoundError newInstance() {
		return new DirichletMultinomialCompoundError(baseConfig, parameters);
	}

	@Override
	public void processCLI(String line) {
		String[] s = line.split(Character.toString(AbstractFilterFactory.SEP));
		// format -u DirMult-CE:epsilon:maxIterations:estimatedError
		for (int i = 1; i < s.length; ++i) {
			switch(i) {

			case 1:
				epsilon = Double.parseDouble(s[i]);
				break;

			case 2:
				maxIterations = Integer.parseInt(s[i]);
				break;

			case 3:
				estimatedError = Double.parseDouble(s[i]);
				break;
				
			default:
				throw new IllegalArgumentException("Invalid argument " + line);
			}
		}
	}

}