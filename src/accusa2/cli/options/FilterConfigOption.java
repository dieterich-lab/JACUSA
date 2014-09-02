package accusa2.cli.options;

import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import accusa2.cli.parameters.AbstractParameters;
import accusa2.filter.factory.AbstractFilterFactory;

public class FilterConfigOption extends AbstractACOption {

	private AbstractParameters parameters;
	
	private static char OR = ',';
	//private static char AND = '&'; // Future Feature add logic

	private Map<Character, AbstractFilterFactory> pileupFilterFactories;

	public FilterConfigOption(AbstractParameters parameters, Map<Character, AbstractFilterFactory> pileupFilterFactories) {
		this.parameters = parameters;
		
		opt = 'a';
		longOpt = "pileup-filter";

		this.pileupFilterFactories = pileupFilterFactories;
	}

	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		StringBuffer sb = new StringBuffer();

		for(char c : pileupFilterFactories.keySet()) {
			AbstractFilterFactory pileupFilterFactory = pileupFilterFactories.get(c);
			sb.append(pileupFilterFactory.getC());
			sb.append(" | ");
			sb.append(pileupFilterFactory.getDesc());
			sb.append("\n");
		}

		return OptionBuilder.withLongOpt(longOpt)
			.withArgName(longOpt.toUpperCase())
			.hasArg(true)
			.withDescription(
					"chain of " + longOpt.toUpperCase() + " to apply to pileups:\n" + sb.toString() + 
					"\nSeparate multiple " + longOpt.toUpperCase() + " with '" + OR + "' (e.g.: D,I)")
			.create(opt); 
	}

	@Override
	public void process(CommandLine line) throws Exception {
		if(line.hasOption(opt)) {
			String s = line.getOptionValue(opt);
			String[] t = s.split(Character.toString(OR));

			for(String a : t) {
				char c = a.charAt(0);
				if(!pileupFilterFactories.containsKey(c)) {
					throw new IllegalArgumentException("Unknown SAM processing: " + c);
				}
				AbstractFilterFactory filterFactory = pileupFilterFactories.get(c);
				if(a.length() > 1) {
					filterFactory.processCLI(a);
				}
				parameters.getFilterConfig().addFactory(filterFactory);
			}
		}
	}

}