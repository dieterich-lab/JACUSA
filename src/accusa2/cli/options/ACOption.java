package accusa2.cli.options;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import accusa2.cli.Parameters;

public interface ACOption {

	public void process(CommandLine line) throws Exception;
	public Option getOption();

	public char getOpt();
	public String getLongOpt();

	Parameters getParameters();
}
