package accusa2.cli.parameters;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import accusa2.ACCUSA2;
import accusa2.cli.options.AbstractACOption;
import accusa2.method.AbstractMethodFactory;

public class CLI {

	private static CLI CLI;
	private Map<String, AbstractMethodFactory> methodFactories;
	private AbstractMethodFactory methodFactory;
	
	/**
	 * 
	 */
	private CLI() {
		this.methodFactories 	= new HashMap<String, AbstractMethodFactory>();
	}

	public static CLI getSingleton() {
		if (CLI == null) {
			CLI = new CLI();
		}

		return CLI;
	}
	
	/**
	 * 
	 * @param methodFactories
	 */
	public void setMethodFactories(Map<String, AbstractMethodFactory> methodFactories) {
		this.methodFactories = methodFactories;
	}

	/**
	 * 
	 * @param args
	 * @return
	 */
	public boolean processArgs(String[] args) {
		if (args.length == 0) {
			printUsage();
			System.exit(0);
		} else if (args.length > 0 && !methodFactories.containsKey(args[0].toLowerCase())) {
			printUsage();
			System.exit(0);
		}
		methodFactory = methodFactories.get(args[0].toLowerCase());
		// init method factory (populate: parameters)
		methodFactory.initACOptions();

		
		Set<AbstractACOption> acoptions = methodFactory.getACOptions();
		Options options = new Options();
		for (AbstractACOption acoption : acoptions) {
			options.addOption(acoption.getOption());
		}

		if (args.length == 1) {
			printUsage(options);
			System.exit(0);
		}
		// copy arguments while ignoring the first array element
		String[] args2 = new String[args.length - 1];
		System.arraycopy(args, 1, args2, 0, args.length - 1);

		// parse arguments
		CommandLineParser parser = new PosixParser();
		try {
			CommandLine line = parser.parse(options, args2);
			for (AbstractACOption acption : acoptions) {
				acption.process(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
			printUsage(options);
			return false;
		}

		return true;
	}

	/**
	 * 
	 * @param options
	 */
	public void printUsage(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.setWidth(160);

		formatter.printHelp("ACCUSA25", options);
	}

	/**
	 * 
	 */
	public void printUsage() {
		StringBuilder sb = new StringBuilder();
		
		for (AbstractMethodFactory methodFactory : methodFactories.values()) {
			sb.append('\t');
			sb.append(methodFactory.getName());
			sb.append('\t');
			sb.append(methodFactory.getDescription());
			sb.append('\n');
		}

		sb.append(ACCUSA2.VERSION + "\n");
		System.err.print(sb.toString());
	}

	public AbstractMethodFactory getMethodFactory() {
		return methodFactory;
	}
	
}