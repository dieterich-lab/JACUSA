package accusa2.cli.options;

import accusa2.cli.Parameters;
import accusa2.cli.options.ACOption;

public abstract class AbstractACOption implements ACOption {

	protected char opt;
	protected String longOpt;
	protected Parameters parameters;

	public AbstractACOption() {
		parameters = new Parameters();
	}

	public AbstractACOption(Parameters parameters) {
		this.parameters = parameters;
	}

	public char getOpt() {
		return opt;
	}

	public String getLongOpt() {
		return longOpt;
	}

	public Parameters getParameters() {
		return parameters;
	}

}
