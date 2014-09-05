package accusa2.cli.options;

import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import accusa2.cli.parameters.AbstractParameters;
import accusa2.io.format.output.AbstractOutputFormat;

public class FormatOption<T extends AbstractOutputFormat> extends AbstractACOption {

	private AbstractParameters parameters;
	private Map<Character, T> formats;

	public FormatOption(AbstractParameters parameters, Map<Character, T> formats) {
		this.parameters = parameters;

		opt = "f";
		longOpt = "output-format";

		this.formats = formats;
	}

	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		StringBuffer sb = new StringBuffer();

		for(char c : formats.keySet()) {
			T format = formats.get(c);
			if(format.getC() == parameters.getFormat().getC()) {
				sb.append("<*>");
			} else {
				sb.append("< >");
			}
			sb.append(" " + c);
			sb.append(": ");
			sb.append(format.getDesc());
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
				if(!formats.containsKey(c)) {
					throw new IllegalArgumentException("Unknown output format: " + c);
				}
				parameters.setFormat(formats.get(c));
			}
		}
	}

}