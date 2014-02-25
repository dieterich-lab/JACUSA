package accusa2.cli.options;


import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import accusa2.cli.Parameters;
import accusa2.method.statistic.StatisticCalculator;

public class StatisticOption extends AbstractACOption {
	
	private Map<String,StatisticCalculator> statistics;

	public StatisticOption(Parameters parameters, Map<String,StatisticCalculator> pileup2Statistic) {
		super(parameters);
		opt = 'u';
		longOpt = "modus";

		this.statistics = pileup2Statistic;
	}

	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		StringBuilder sb = new StringBuilder();

		for(String name : statistics.keySet()) {
			StatisticCalculator statistic = statistics.get(name);

			if(parameters.getStatisticCalculator() != null && statistic.getName().equals(parameters.getStatisticCalculator().getName())) {
				sb.append("<*>");
			} else {
				sb.append("< >");
			}
			sb.append(" " + name);
			sb.append(": ");
			sb.append(statistic.getDescription());
			sb.append("\n");
		}

		return OptionBuilder.withLongOpt(longOpt)
			.withArgName(longOpt.toUpperCase())
			.hasArg(true)
			.withDescription("Choose between different modes:\n" + sb.toString())
			.create(opt);
	}

	@Override
	public void process(CommandLine line) throws Exception {
		if(line.hasOption(opt)) {
			String name = line.getOptionValue(opt);
			if(! statistics.containsKey(name)) {
				throw new IllegalArgumentException("Unknown statistic: " + name);
			}
			parameters.setStatistic(statistics.get(name));
		}
	}

}
