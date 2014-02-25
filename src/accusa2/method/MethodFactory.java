package accusa2.method;

import java.util.Set;

import accusa2.cli.Parameters;
import accusa2.cli.options.ACOption;
import accusa2.method.statistic.StatisticCalculator;

public interface MethodFactory {

	public Set<ACOption> registerACOptions(Parameters parameters);
	public StatisticCalculator getStatistic();

	public String getName(); 
	public String getDescription();

}
