package accusa2.cli.options;


import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import accusa2.cli.Parameters;
import accusa2.io.format.AbstractResultFormat;

public class ResultFormatOption extends AbstractACOption {

	private Map<Character, AbstractResultFormat> resultFormats;

	public ResultFormatOption(Parameters parameters, Map<Character, AbstractResultFormat> resultFormats) {
		super(parameters);
		opt = 'f';
		longOpt = "output-format";

		this.resultFormats = resultFormats;
	}

	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		StringBuffer sb = new StringBuffer();

		for(char c : resultFormats.keySet()) {
			AbstractResultFormat resultFormat = resultFormats.get(c);
			if(resultFormat.getC() == parameters.getResultFormat().getC()) {
				sb.append("<*>");
			} else {
				sb.append("< >");
			}
			sb.append(" " + c);
			sb.append(": ");
			sb.append(resultFormat.getDesc());
			sb.append("\n");
		}
		
		return OptionBuilder.withLongOpt(longOpt)
			.withArgName(longOpt.toUpperCase())
			.hasArg(true)
			.withDescription("Choose output format:\n" + sb.toString())
			.create(opt); 
	}

	@Override
	public void process(CommandLine line) throws Exception {
		if(line.hasOption(opt)) {
			String s = line.getOptionValue(opt);
			for(int i = 0; i < s.length(); ++i) {
				char c = s.charAt(i);
				if(!resultFormats.containsKey(c)) {
					throw new IllegalArgumentException("Unknown output format: " + c);
				}
				parameters.setResultFormat(resultFormats.get(c));
			}
		}
	}

}