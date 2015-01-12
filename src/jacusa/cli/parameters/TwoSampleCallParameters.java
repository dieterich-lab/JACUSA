package jacusa.cli.parameters;

import jacusa.io.format.result.AbstractResultFormat;
import jacusa.io.format.result.DefaultResultFormat;
import jacusa.method.call.statistic.lr.LR2Statistic;

public class TwoSampleCallParameters extends AbstractParameters implements hasSampleB, hasStatisticCalculator, hasResultFormat {

	private SampleParameters sampleB;
	private StatisticParameters statisticParameters;

	public TwoSampleCallParameters() {
		super();

		sampleB				= new SampleParameters();
		statisticParameters = new StatisticParameters();
		statisticParameters.setStatisticCalculator(new LR2Statistic(getBaseConfig(), statisticParameters));

		super.setFormat(new DefaultResultFormat(getBaseConfig(), getFilterConfig()));
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
	public AbstractResultFormat getFormat() {
		return (AbstractResultFormat)super.getFormat();
	}

	@Override
	public void setFormat(AbstractResultFormat format) {
		super.setFormat(format);
	}

}