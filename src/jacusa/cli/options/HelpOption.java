package jacusa.cli.options;

import jacusa.cli.parameters.CLI;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

public class HelpOption extends AbstractACOption {

	private CLI cmd;
	
	public HelpOption(CLI cmd) {
		opt = "h";
		longOpt = "help";

		this.cmd = cmd;
	}

	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		return OptionBuilder.withLongOpt(longOpt)
				.hasArg(false)
				.withDescription("Print usage information")
				.create(opt);
	}

	@Override
	public void process(CommandLine line) {
		if (line.hasOption(opt)) {
	    	cmd.printUsage(); 
	    	System.exit(0);
	    }
	}

}