package jacusa.method.call.statistic.dirmult;

import jacusa.cli.parameters.StatisticParameters;
import jacusa.filter.factory.AbstractFilterFactory;
import jacusa.method.call.statistic.StatisticCalculator;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.Pileup;
import jacusa.util.MathUtil;

import java.util.Arrays;


public class DirichletMultinomialEstimatedError extends AbstractDirMultStatistic {

	private double estimatedError = 0.01;
	
	public DirichletMultinomialEstimatedError(final BaseConfig baseConfig, final StatisticParameters parameters) {
		super(baseConfig, parameters);
	}

	@Override
	public String getName() {
		return "DirMult-EE";
	}

	@Override
	public String getDescription() {
		return "(phred score IGNORED) Estimated Err. {" + estimatedError + "}"; 
				//" (DirMult-EE:epsilon=<epsilon>:maxIterations=<maxIterations>:estimatedError=<estimatedError>)";
	}

	protected void populate(final Pileup[] pileups, final int[] baseIs, double[] pileupCoverages, double[][] pileupMatrix) {
		// init
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
	}

	@Override
	protected void populate(final Pileup pileup, final int[] baseIs, double[] pileupCoverage, double[] pileupErrorVector, double[] pileupMatrix) {
		// init
		pileupCoverage[0] = 0.0;
		Arrays.fill(pileupMatrix, 0.0);

		double[] pileupCount = phred2Prob.colSumCount(baseIs, pileup);
		pileupErrorVector = phred2Prob.colMeanErrorProb(baseIs, pileup);

		for (int baseI : baseIs) {
			pileupMatrix[baseI] += pileupCount[baseI];
			if (pileupCount[baseI] > 0.0) {
				for (int baseI2 : baseIs) {
					if (baseI != baseI2) {
						double error = (pileupErrorVector[baseI2]) * (double)pileupCount[baseI] / (double)(baseIs.length - 1);
						pileupMatrix[baseI2] += error;
						pileupErrorVector[baseI2] = error;
					}
				}
			}
		}
		pileupCoverage[0] = MathUtil.sum(pileupMatrix);
	}
	
	@Override
	public StatisticCalculator newInstance() {
		return new DirichletMultinomialEstimatedError(baseConfig, parameters);
	}

	// format -u DirMult:epsilon=<epsilon>:maxIterations=<maxIterions>:estimatedError=<estimatedError>
	@Override
	public boolean processCLI(String line) {
		boolean r = super.processCLI(line);

		String[] s = line.split(Character.toString(AbstractFilterFactory.SEP));
		for (int i = 1; i < s.length; ++i) {
			// key=value
			String[] kv = s[i].split("=");
			String key = kv[0];
			String value = new String();
			if (kv.length == 2) {
				value = kv[1];
			}

			// set value
			if (key.equals("estimatedError")) {
				estimatedError = Double.parseDouble(value);
				r = true;
			} else if (!r){
				throw new IllegalArgumentException("Invalid argument " + key + " IN: " + line);
			}
		}

		return r;
	}

}