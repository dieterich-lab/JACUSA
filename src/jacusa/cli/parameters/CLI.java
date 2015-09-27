package jacusa.cli.parameters;


import jacusa.JACUSA;
import jacusa.cli.options.AbstractACOption;
import jacusa.io.format.VCF_ResultFormat;
import jacusa.method.AbstractMethodFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

public class CLI {

	// singleton
	private static CLI CLI;

	private Map<String, AbstractMethodFactory> methodFactories;
	private AbstractMethodFactory methodFactory;

	/**
	 * 
	 */
	private CLI() {
		this.methodFactories = new HashMap<String, AbstractMethodFactory>();
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
		} else if (args.length > 0 && ! methodFactories.containsKey(args[0].toLowerCase())) {
			printUsage();
			System.exit(0);
		}
		methodFactory = methodFactories.get(args[0].toLowerCase());
		// init method factory (populate: parameters)
		methodFactory.initACOptions();

		Set<AbstractACOption> acOptions = methodFactory.getACOptions();
		Options options = new Options();
		for (AbstractACOption acoption : acOptions) {
			options.addOption(acoption.getOption());
		}

		if (args.length == 1) {
			methodFactory.printUsage();
			System.exit(0);
		}
		// copy arguments while ignoring the first array element
		String[] processedArgs = new String[args.length - 1];
		System.arraycopy(args, 1, processedArgs, 0, args.length - 1);

		// parse arguments
		CommandLineParser parser = new PosixParser();
		try {
			CommandLine line = parser.parse(options, processedArgs);
			for (AbstractACOption acption : acOptions) {
				acption.process(line);
			}
			methodFactory.parseArgs(line.getArgs());
		} catch (Exception e) {
			e.printStackTrace();
			methodFactory.printUsage();
			return false;
		}

		// check stranded and VCF chosen
		if (methodFactory.getParameters().getFormat().getC() == VCF_ResultFormat.CHAR) {
			boolean error = false;
			if(methodFactory.getParameters() instanceof hasSample1) {
				if (methodFactory.getParameters().getSample1().getPileupBuilderFactory().isDirected()) {
					error = true;
				}
			}
			if(methodFactory.getParameters() instanceof hasSample2) {
				if (((hasSample2)methodFactory.getParameters()).getSample2().getPileupBuilderFactory().isDirected()) {
					error = true;
				}
			}
			
			if (error) {
				System.err.println("ERROR: Output format VCF does not supported stranded Pileup Builder!");
				System.err.println("ERROR: Change output format or use unstranded Pileup Builder!");
				System.exit(0);
			}
		}

		return true;
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

		sb.append(JACUSA.VERSION + "\n");
		System.err.print(sb.toString());
	}

	public AbstractMethodFactory getMethodFactory() {
		return methodFactory;
	}

}