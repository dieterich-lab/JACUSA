package accusa2.cli.parameters;

import accusa2.io.format.result.AbstractResultFormat;
import accusa2.io.format.result.DefaultResultFormat;

public class TwoSampleCallParameters extends AbstractParameters implements hasSampleB, hasStatisticCalculator, hasResultFormat {

	private SampleParameters sampleB;
	private StatisticParameters statisticParameters;
	private AbstractResultFormat format;

	public TwoSampleCallParameters() {
		super();

		sampleB				= new SampleParameters();
		statisticParameters = new StatisticParameters();
		format		= new DefaultResultFormat(getBaseConfig());
	}

	@Override
	public SampleParameters getSampleB() {
		return sampleB;
	}

	@Override
	public StatisticParameters getStatisticParameters() {
		return statisticParameters;
	}

	public AbstractResultFormat getFormat() {
		return format;
	}

	public void setFormat(AbstractResultFormat format) {
		this.format = format;
	}

}