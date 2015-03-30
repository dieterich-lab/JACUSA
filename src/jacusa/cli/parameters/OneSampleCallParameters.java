package jacusa.cli.parameters;

import jacusa.io.format.AbstractOutputFormat;

public class OneSampleCallParameters extends AbstractParameters implements hasStatisticCalculator {

	private StatisticParameters statisticParameters;
	private AbstractOutputFormat format;

	public OneSampleCallParameters() {
		super();

		statisticParameters = new StatisticParameters();
	}

	@Override
	public StatisticParameters getStatisticParameters() {
		return statisticParameters;
	}

	@Override
	public void setFormat(AbstractOutputFormat format) {
		this.format = format;
	}
	
	@Override
	public AbstractOutputFormat getFormat() {
		return format;
	}
	
}