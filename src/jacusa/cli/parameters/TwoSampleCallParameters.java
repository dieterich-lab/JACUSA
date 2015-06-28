package jacusa.cli.parameters;

import jacusa.io.format.AbstractOutputFormat;
import jacusa.io.format.DefaultOutputFormat;
import jacusa.method.call.statistic.dirmult.DirichletMultinomialCompoundError;

public class TwoSampleCallParameters extends AbstractParameters implements hasSampleB, hasStatisticCalculator {
	private SampleParameters sampleB;
	private StatisticParameters statisticParameters;

	public TwoSampleCallParameters() {
		super();

		sampleB				= new SampleParameters();
		statisticParameters = new StatisticParameters();
		statisticParameters.setStatisticCalculator(new DirichletMultinomialCompoundError(getBaseConfig(), statisticParameters));

		super.setFormat(new DefaultOutputFormat(getBaseConfig(), getFilterConfig()));
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

	@Override
	public void setFormat(AbstractOutputFormat format) {
		super.setFormat(format);
	}

}