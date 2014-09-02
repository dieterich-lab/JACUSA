package accusa2.cli.parameters;

public class OneSampleCallParameters extends AbstractParameters implements hasStatisticCalculator {

	private StatisticParameters statisticParameters;
	
	public OneSampleCallParameters() {
		super();

		statisticParameters = new StatisticParameters();
	}

	@Override
	public StatisticParameters getStatisticParameters() {
		return statisticParameters;
	}
	
}
