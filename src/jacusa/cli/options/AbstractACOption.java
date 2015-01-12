package jacusa.cli.options;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

public abstract class AbstractACOption {

	protected String opt;
	protected String longOpt;

	public AbstractACOption() {
		
	}

	public abstract void process(CommandLine line) throws Exception;
	public abstract Option getOption();
	
	public String getOpt() {
		return opt;
	}

	public String getLongOpt() {
		return longOpt;
	}

}