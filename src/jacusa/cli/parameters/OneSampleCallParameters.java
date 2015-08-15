package jacusa.cli.parameters;

import jacusa.io.format.AbstractOutputFormat;
import jacusa.method.call.statistic.dirmult.DirichletMultinomialCompoundError;

public class OneSampleCallParameters extends AbstractParameters implements hasStatisticCalculator {

	private StatisticParameters statisticParameters;
	private AbstractOutputFormat format;

	public OneSampleCallParameters() {
		super();

		statisticParameters = new StatisticParameters();
		statisticParameters.setStatisticCalculator(new DirichletMultinomialCompoundError(getBaseConfig(), statisticParameters));
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