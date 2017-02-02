package jacusa.cli.parameters;

import jacusa.io.format.AbstractOutputFormat;
import jacusa.method.call.statistic.dirmult.DirichletMultinomialRobustCompoundError;

public class TwoSampleCallParameters extends AbstractParameters implements hasSample2, hasStatisticCalculator {
	private SampleParameters sampleB;
	private StatisticParameters statisticParameters;

	public TwoSampleCallParameters() {
		super();

		sampleB				= new SampleParameters();
		statisticParameters = new StatisticParameters();
		statisticParameters.setStatisticCalculator(new DirichletMultinomialRobustCompoundError(getBaseConfig(), statisticParameters));
	}

	@Override
	public SampleParameters getSample2() {
		return sampleB;
	}

	@Override
	public StatisticParameters getStatisticParameters() {
		return statisticParameters;
	}

	@Override
	public AbstractOutputFormat getFormat() {
		return (AbstractOutputFormat)super.getFormat();
	}

}