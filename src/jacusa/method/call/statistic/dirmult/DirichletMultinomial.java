package jacusa.method.call.statistic.dirmult;

import jacusa.cli.parameters.StatisticParameters;
import jacusa.method.call.statistic.StatisticCalculator;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.Pileup;
import jacusa.util.MathUtil;

import java.util.Arrays;

public class DirichletMultinomial extends AbstractDirMultStatistic {

	public DirichletMultinomial(final BaseConfig baseConfig, final StatisticParameters parameters) {
		super(baseConfig, parameters);
	}

	@Override
	protected void populate(final Pileup[] pileups, final int[] baseIs, double[] pileupCoverages, double[][] pileupMatrix) {
		// init
		Arrays.fill(pileupCoverages, 0.0);
		for (int i = 0; i < pileupMatrix.length; ++i) {
			Arrays.fill(pileupMatrix[i], 0.0);
		}

		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			Pileup pileup = pileups[pileupI];
			pileupMatrix[pileupI] = phred2Prob.colSumProb(baseIs, pileup);

			pileupCoverages[pileupI] = MathUtil.sum(pileupMatrix[pileupI]);
		}
	}

	@Override
	public StatisticCalculator newInstance() {
		return new DirichletMultinomial(baseConfig, parameters);
	}

	@Override
	public String getName() {
		return "DirMult";
	}

	@Override
	public String getDescription() {
		return "Dirichlet-Multinomial - Only Phred score (DirMult:epsilon=<epsilon>:maxIterations=<maxIterations>)";
	}

}