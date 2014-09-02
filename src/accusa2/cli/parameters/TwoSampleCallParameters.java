package accusa2.cli.parameters;

import accusa2.io.format.result.AbstractResultFormat;
import accusa2.io.format.result.DefaultResultFormat;

public class TwoSampleCallParameters extends AbstractParameters implements hasSampleB, hasStatisticCalculator {

	private SampleParameters sampleB;
	private StatisticParameters statisticParameters;
	private AbstractResultFormat resultFormat;

	public TwoSampleCallParameters() {
		super();

		sampleB				= new SampleParameters();
		statisticParameters = new StatisticParameters();
		resultFormat		= new DefaultResultFormat(this);
	}

	@Override
	public SampleParameters getSampleB() {
		return sampleB;
	}

	@Override
	public StatisticParameters getStatisticParameters() {
		return statisticParameters;
	}

	public AbstractResultFormat getResultFormat() {
		return resultFormat;
	}

}