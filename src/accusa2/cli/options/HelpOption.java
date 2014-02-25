package accusa2.cli.options;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import accusa2.cli.CLI;
import accusa2.cli.Parameters;

public class HelpOption extends AbstractACOption {

	private CLI cmd;
	
	public HelpOption(Parameters parameters, CLI cmd) {
		super(parameters);
		opt = 'h';
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
		if(line.hasOption(opt)) {
	    	cmd.printUsage(); 
	    	System.exit(0);
	    }
	}

}
