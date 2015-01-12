package jacusa.cli.parameters;

import jacusa.io.format.result.AbstractResultFormat;

public class OneSampleCallParameters extends AbstractParameters implements hasStatisticCalculator, hasResultFormat {

	private StatisticParameters statisticParameters;
	private AbstractResultFormat format;

	public OneSampleCallParameters() {
		super();

		statisticParameters = new StatisticParameters();
	}

	@Override
	public StatisticParameters getStatisticParameters() {
		return statisticParameters;
	}

	@Override
	public void setFormat(AbstractResultFormat format) {
		this.format = format;
	}
	
	@Override
	public AbstractResultFormat getFormat() {
		return format;
	}
	
}