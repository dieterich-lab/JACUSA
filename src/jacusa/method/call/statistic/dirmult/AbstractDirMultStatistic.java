package jacusa.method.call.statistic.dirmult;


import jacusa.cli.parameters.StatisticParameters;
import jacusa.estimate.MinkaEstimateParameters;
import jacusa.filter.factory.AbstractFilterFactory;
import jacusa.method.call.statistic.StatisticCalculator;
import jacusa.method.call.statistic.dirmult.initalpha.AbstractAlphaInit;
import jacusa.method.call.statistic.dirmult.initalpha.BayesAlphaInit;
import jacusa.method.call.statistic.dirmult.initalpha.CombinedAlphaInit;
import jacusa.method.call.statistic.dirmult.initalpha.ConstantAlphaInit;
import jacusa.method.call.statistic.dirmult.initalpha.MeanAlphaInit;
import jacusa.method.call.statistic.dirmult.initalpha.RonningAlphaInit;
import jacusa.phred2prob.Phred2Prob;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Pileup;
import jacusa.pileup.Result;

import java.text.DecimalFormat;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;

public abstract class AbstractDirMultStatistic implements StatisticCalculator {

	protected final StatisticParameters parameters;
	protected final BaseConfig baseConfig;
	protected Phred2Prob phred2Prob;

	protected boolean onlyObservedBases;
	protected boolean showAlpha;
	protected boolean calcPValue;
	
	protected double[] alpha1;
	protected double[] alpha2;
	protected double[] alphaP;

	protected double[] initAlpha1;
	protected double[] initAlpha2;
	protected double[] initAlphaP;
	
	protected int iterations1;
	protected int iterations2;
	protected int iterationsP;
	protected double logLikelihood1;
	protected double logLikelihood2;
	protected double logLikelihoodP;
	
	protected MinkaEstimateParameters estimateAlpha;

	public AbstractDirMultStatistic(final BaseConfig baseConfig, final StatisticParameters parameters) {
		this.parameters 	= parameters;
		final int n 		= baseConfig.getBaseLength();
		this.baseConfig 	= baseConfig;
		phred2Prob 			= Phred2Prob.getInstance(n);
		onlyObservedBases 	= false;
		showAlpha			= false;

		estimateAlpha		= new MinkaEstimateParameters();
	}

	protected abstract void populate(
			final Pileup[] pileups, 
			final int[] baseIs, 
			double[] pileupCoverages, 
			double[][] pileupMatrix);

	protected abstract void populate(
			final Pileup pileup, 
			final int[] baseIs, 
			double[] pileupCoverage,
			double[] pileupErrorVector,
			double[] pileupVector);
	
	@Override
	public synchronized void addStatistic(Result result) {
		final double statistic = getStatistic(result.getParellelPileup());
		result.setStatistic(statistic);

		if (showAlpha) {
			DecimalFormat df = new DecimalFormat("0.00"); 
			StringBuilder sb = new StringBuilder();
			sb.append("alpha1=");
			sb.append(df.format(alpha1[0]));
			for (int i = 1; i < alpha1.length; ++i) {
				sb.append(":");
				sb.append(df.format(alpha1[i]));
			}
			sb.append(";alpha2=");
			sb.append(df.format(alpha2[0]));
			for (int i = 1; i < alpha2.length; ++i) {
				sb.append(":");
				sb.append(df.format(alpha2[i]));
			}
			sb.append(";alphaP=");
			sb.append(df.format(alphaP[0]));
			for (int i = 1; i < alphaP.length; ++i) {
				sb.append(":");
				sb.append(df.format(alphaP[i]));
			}

			sb.append(";initAlpha1=");
			sb.append(df.format(initAlpha1[0]));
			for (int i = 1; i < initAlpha1.length; ++i) {
				sb.append(":");
				sb.append(df.format(initAlpha1[i]));
			}
			sb.append(";initAlpha2=");
			sb.append(df.format(initAlpha2[0]));
			for (int i = 1; i < initAlpha2.length; ++i) {
				sb.append(":");
				sb.append(df.format(initAlpha2[i]));
			}
			sb.append(";initAlphaP=");
			sb.append(df.format(initAlphaP[0]));
			for (int i = 1; i < initAlphaP.length; ++i) {
				sb.append(":");
				sb.append(df.format(initAlphaP[i]));
			}
			
			sb.append(";");
			sb.append("logLikelihood1=");
			sb.append(logLikelihood1);
			sb.append(";");
			sb.append("logLikelihood2=");
			sb.append(logLikelihood2);
			sb.append(";");
			sb.append("logLikelihoodP=");
			sb.append(logLikelihoodP);
			sb.append(";");
			sb.append("iterations1=");
			sb.append(iterations1);
			sb.append(";");
			sb.append("iterations2=");
			sb.append(iterations2);
			sb.append(";");
			sb.append("iterationsP=");
			sb.append(iterationsP);
			
			result.addInfo(sb.toString());
		}
	}
	
	@Override
	public double getStatistic(final ParallelPileup parallelPileup) {
		final int baseIs[] = getBaseIs(parallelPileup);
		int baseN = baseConfig.getBaseLength();

		ChiSquareDist dist = new ChiSquareDist(baseN - 1);

		alpha1 = new double[baseN];
		double[] pileupCoverages1 = new double[parallelPileup.getN1()];
		double[][] pileupMatrix1  = new double[parallelPileup.getN1()][baseN];
		
		alpha2 = new double[baseN];
		double[] pileupCoverages2 = new double[parallelPileup.getN2()];
		double[][] pileupMatrix2 = new double[parallelPileup.getN2()][baseN];

		alphaP = new double[baseN];
		double[] pileupCoveragesP = new double[parallelPileup.getN()];
		double[][] pileupMatrixP = new double[parallelPileup.getN()][baseN];

		if (parallelPileup.getPileups1().length == 1) {
			double[] pileupErrorVector1 = new double[baseN];
			populate(parallelPileup.getPileups1()[0], baseIs, pileupCoverages1, pileupErrorVector1, pileupMatrix1[0]);
			initAlpha1 = estimateAlpha.getAlphaInit().init(baseIs, parallelPileup.getPileups1()[0], pileupMatrix1[0], pileupErrorVector1, pileupCoverages1[0]);
		} else {
			populate(parallelPileup.getPileups1(), baseIs, pileupCoverages1, pileupMatrix1);
			initAlpha1 = estimateAlpha.getAlphaInit().init(baseIs, parallelPileup.getPileups1(), pileupMatrix1, pileupCoverages1);
		}
		System.arraycopy(initAlpha1, 0, alpha1, 0, baseN);
		if (parallelPileup.getPileups2().length == 1) {
			double[] pileupErrorVector2 = new double[baseN];
			populate(parallelPileup.getPileups2()[0], baseIs, pileupCoverages2, pileupErrorVector2, pileupMatrix2[0]);
			initAlpha2 = estimateAlpha.getAlphaInit().init(baseIs, parallelPileup.getPileups2()[0], pileupMatrix2[0], pileupErrorVector2, pileupCoverages2[0]);
		} else {
			populate(parallelPileup.getPileups2(), baseIs, pileupCoverages2, pileupMatrix2);
			initAlpha2 = estimateAlpha.getAlphaInit().init(baseIs, parallelPileup.getPileups2(), pileupMatrix2, pileupCoverages2);
		}
		System.arraycopy(initAlpha2, 0, alpha2, 0, baseN);
		populate(parallelPileup.getPileupsP(), baseIs, pileupCoveragesP, pileupMatrixP);
		initAlphaP = estimateAlpha.getAlphaInit().init(baseIs, parallelPileup.getPileupsP(), pileupMatrixP, pileupCoveragesP);
		System.arraycopy(initAlphaP, 0, alphaP, 0, baseN);
		
		double stat = Double.NaN;
		try {
			// estimate alphas

			logLikelihood1 = estimateAlpha.maximizeLogLikelihood(baseIs, alpha1, pileupCoverages1, pileupMatrix1);
			iterations1 = estimateAlpha.getIterations();
			logLikelihood2 = estimateAlpha.maximizeLogLikelihood(baseIs, alpha2, pileupCoverages2, pileupMatrix2);
			iterations2 = estimateAlpha.getIterations();
			logLikelihoodP = estimateAlpha.maximizeLogLikelihood(baseIs, alphaP, pileupCoveragesP, pileupMatrixP);
			iterationsP = estimateAlpha.getIterations();

			if (calcPValue) {
				stat = -2 * (logLikelihoodP - (logLikelihood1 + logLikelihood2));
				stat = 1 - dist.cdf(stat);
			} else {
				stat = (logLikelihood1 + logLikelihood2) - logLikelihoodP;
			}
		} catch (StackOverflowError e) {
			System.out.println("Warning: Numerical Stability");
			System.out.println(parallelPileup.getContig());
			System.out.println(parallelPileup.getStart());
			System.out.println(parallelPileup.prettyPrint());

			return stat;
		}

		return stat;
	}

	// Debug function
	protected void printAlpha(double[] alphas) {
		StringBuilder sb = new StringBuilder();
		for (double alpha : alphas) {
			sb.append(Double.toString(alpha));
			sb.append("\t");
		}
		System.out.println(sb.toString());
	}

	@Override
	public boolean filter(double value) {
		if (calcPValue) {
			return parameters.getThreshold() < value;
		}
		if (parameters.getThreshold() == Double.NaN) {
			return false;
		}
		
		return value < parameters.getThreshold();
	}

	// format -u DirMult:epsilon=<epsilon>:maxIterations=<maxIterions>:onlyObserved
	@Override
	public boolean processCLI(String line) {
		String[] s = line.split(Character.toString(AbstractFilterFactory.SEP));
		boolean r = false;

		for (int i = 1; i < s.length; ++i) {
			// key=value
			String[] kv = s[i].split("=");
			String key = kv[0];
			String value = new String();
			if (kv.length == 2) {
				value = kv[1];
			}

			// set value
			if (key.equals("epsilon")) {
				estimateAlpha.setEpsilon(Double.parseDouble(value));
				r = true;
			} else if(key.equals("maxIterations")) {
				estimateAlpha.setMaxIterations(Integer.parseInt(value));
				r = true;
			} else if(key.equals("onlyObserved")) {
				onlyObservedBases = true;
				r = true;
			} else if(key.equals("calculateP-value")) {
				calcPValue = true;
			} else if(key.equals("showAlpha")) {
				showAlpha = true;
				r = true;
			} else if(key.equals("initAlpha")) {
				// ugly
				String initAlphaClass = value.split(Character.toString(','))[0];
				AbstractAlphaInit alphaInit = null;
				if (initAlphaClass.equals("bayes")) {
					alphaInit = new BayesAlphaInit();
				} else if (initAlphaClass.equals("combined")) {
					alphaInit = new CombinedAlphaInit();
				} else if (initAlphaClass.equals("constant")) {
					double constant = -1d;
					for (String v : value.split(Character.toString(','))) {
						String[] kv2 = v.split("=");
						String key2 = kv[0];
						String value2 = new String();
						if (kv2.length == 2) {
							value2 = kv2[1];
						}
						if (key2.equals("value")) {
							constant = Double.parseDouble(value2);
						}
					}
					if (constant == -1d) {
						throw new IllegalArgumentException(line + "\nConstant has to be > 0");
					}
					alphaInit = new ConstantAlphaInit(constant);
				} else if (initAlphaClass.equals("mean")) {
					alphaInit = new MeanAlphaInit();
				} else if (initAlphaClass.equals("Ronning")) {
					alphaInit = new RonningAlphaInit();
				} else {
					throw new IllegalArgumentException("Unknown initAlpha: " + value);
				}

				estimateAlpha.setAlphaInit(alphaInit);
				r = true;
			}
		}

		return r;
	}

	public int[] getBaseIs(ParallelPileup parallelPileup) {
		if (onlyObservedBases) {
			return parallelPileup.getPooledPileup().getAlleles();
		}

		return baseConfig.getBasesI();
	}

}