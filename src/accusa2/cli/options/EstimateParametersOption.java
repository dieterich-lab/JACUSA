package accusa2.cli.options;

import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import accusa2.cli.options.AbstractACOption;
import accusa2.cli.parameters.StatisticParameters;
import accusa2.estimate.AbstractEstimateParameters;

public class EstimateParametersOption extends AbstractACOption {

	private StatisticParameters parameters;
	private Map<String, AbstractEstimateParameters> estimators;

	public EstimateParametersOption(final StatisticParameters parameters, Map<String, AbstractEstimateParameters> estimators) {
		this.parameters = parameters;

		opt = "e";
		longOpt = "estimate";

		this.estimators = estimators;
	}

	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		StringBuilder sb = new StringBuilder();

		for (String name : estimators.keySet()) {
			AbstractEstimateParameters estimator = estimators.get(name);

			if (parameters.getEstimateParameters() != null && estimator.getName().equals(parameters.getEstimateParameters().getName())) {
				sb.append("<*>");
			} else {
				sb.append("< >");
			}
			sb.append(" " + name);
			sb.append(": ");
			sb.append(estimator.getDesc());
			sb.append("\n");
		}

		return OptionBuilder.withLongOpt(longOpt)
			.withArgName(longOpt.toUpperCase())
			.hasArg(true)
			.withDescription("Choose between different estimators:\n" + sb.toString())
			.create(opt);
	}

	@Override
	public void process(CommandLine line) throws Exception {
		if (line.hasOption(opt)) {
			String name = line.getOptionValue(opt);
			if (! estimators.containsKey(name)) {
				throw new IllegalArgumentException("Unknown estimtator: " + name);
			}
			parameters.setEstimateParameters(estimators.get(name));
		}
	}

}