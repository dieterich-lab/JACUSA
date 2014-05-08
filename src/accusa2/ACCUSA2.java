package accusa2;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMSequenceRecord;
import accusa2.cli.CLI;
import accusa2.cli.Parameters;
import accusa2.method.ACCUSA25Factory;
import accusa2.method.AbstractMethodFactory;
//import accusa2.method.ACCUSA2Factory;
import accusa2.method.PileupFactory;
import accusa2.process.parallelpileup.dispatcher.AbstractParallelPileupWorkerDispatcher;
import accusa2.process.parallelpileup.worker.AbstractParallelPileupWorker;
import accusa2.util.AnnotatedCoordinate;
import accusa2.util.BEDCoordinateProvider;
import accusa2.util.CoordinateProvider;
import accusa2.util.SAMCoordinateProvider;
import accusa2.util.SimpleTimer;

/**
 * @author Michael Piechotta
 */
public class ACCUSA2 {

	// timer used for all time measurements
	private static SimpleTimer timer;
	
	// command line interface
	private CLI cli;

	/**
	 * 
	 */
	public ACCUSA2() {
		cli = CLI.getSingleton();

		// container for available methods (e.g.: call, pileup)
		Map<String, AbstractMethodFactory> methodFactories = new TreeMap<String, AbstractMethodFactory>();

		AbstractMethodFactory methodFactory = null;

		// instantiate different methods
		methodFactory = new ACCUSA25Factory();
		methodFactories.put(methodFactory.getName(), methodFactory);

		methodFactory = new PileupFactory();
		methodFactories.put(methodFactory.getName(), methodFactory);

		// add to cli 
		cli.setMethodFactories(methodFactories);
	}

	/**
	 * Singleton Pattern
	 * @return a SimpleTimer instance
	 */
	public static SimpleTimer getSimpleTimer() {
		if(timer == null) {
			timer = new SimpleTimer();
		}

		return timer;
	}

	/**
	 * 
	 * @return
	 */
	public CLI getCLI() {
		return cli;
	}

	/**
	 * 
	 * @param pathnames1
	 * @param pathnames2
	 * @return
	 * @throws Exception
	 */
	public List<SAMSequenceRecord> getSAMSequenceRecords(String[] pathnames1, String[] pathnames2) throws Exception {
		printLog("Computing overlap between sequence records.");
		String error = "Sequence Dictionary of BAM files do not match";

		SAMFileReader reader 				= new SAMFileReader(new File(pathnames1[0]));
		List<SAMSequenceRecord> records 	= reader.getFileHeader().getSequenceDictionary().getSequences();
		// close readers
		reader.close();

		List<AnnotatedCoordinate> coordinates = new ArrayList<AnnotatedCoordinate>();
		Set<String> targetSequenceNames = new HashSet<String>();
		for(SAMSequenceRecord record : records) {
			coordinates.add(new AnnotatedCoordinate(record.getSequenceName(), 1, record.getSequenceLength()));
			targetSequenceNames.add(record.getSequenceName());
		}

		if(!isValid(targetSequenceNames, pathnames1) || !isValid(targetSequenceNames, pathnames2)) {
			throw new Exception(error);
		}

		return records;
	}

	private boolean isValid(Set<String> targetSequenceNames, String[] pathnames) {
		Set<String> sequenceNames = new HashSet<String>();
		for(String pathname : pathnames) {
			SAMFileReader reader = new SAMFileReader(new File(pathname));
			List<SAMSequenceRecord> records	= reader.getFileHeader().getSequenceDictionary().getSequences();
			for(SAMSequenceRecord record : records) {
				sequenceNames.add(record.getSequenceName());
			}	
			reader.close();
		}
		
		if(!sequenceNames.containsAll(targetSequenceNames) || !targetSequenceNames.containsAll(sequenceNames)) {
			return false;
		}

		return true;
	}

	/**
	 * 
	 * @param size
	 * @param args
	 */
	public void printProlog(int size, String[] args) {
		String lineSep = "--------------------------------------------------------------------------------";

		System.err.println(lineSep);

		StringBuilder sb = new StringBuilder();
		sb.append("ACCUSA25");
		for(String arg : args) {
			sb.append(" " + arg);
		}
		System.err.println(sb.toString());

		System.err.println(lineSep);
	}

	/**
	 * 
	 * @param line
	 */
	public static void printLog(String line) {
		String time = "[ " + getSimpleTimer().getTotalTimestring() + " ]";
		System.err.println(time + " " + line);
	}

	/**
	 * 
	 * @param comparisons
	 */
	public void printEpilog(int comparisons) {
		// print statistics to STDERR
		printLog("Screening done using " + cli.getParameters().getMaxThreads() + " thread(s)");

		System.err.println("Results can be found in: " + cli.getParameters().getOutput().getInfo());

		String lineSep = "--------------------------------------------------------------------------------";

		System.err.println(lineSep);
		System.err.println("Analyzed Parallel Pileups:\t" + comparisons);
		System.err.println("Elapsed time:\t\t\t" + getSimpleTimer().getTotalTimestring());
	}

	/**
	 * Application logic.
	 * 
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		ACCUSA2 accusa2 = new ACCUSA2();
		CLI cmd = accusa2.getCLI();

		if(!cmd.processArgs(args)) {
			System.exit(1);
		}
		Parameters parameters = cmd.getParameters();

		String[] pathnames1 = parameters.getPathnames1();
		String[] pathnames2 = parameters.getPathnames2();
		List<SAMSequenceRecord> records = accusa2.getSAMSequenceRecords(pathnames1, pathnames2);
		CoordinateProvider coordinateProvider = new SAMCoordinateProvider(records);

		if(!parameters.getBED_Pathname().isEmpty()) {
			coordinateProvider.close();
			// BED file
			coordinateProvider = new BEDCoordinateProvider(parameters.getBED_Pathname());
		}

		// prolog
		accusa2.printProlog(records.size(), args);
		// main
		AbstractParallelPileupWorkerDispatcher<? extends AbstractParallelPileupWorker> threadDispatcher = parameters.getMethodFactory().getInstance(coordinateProvider, parameters);
		int comparisons = threadDispatcher.run();
		// epilog
		accusa2.printEpilog(comparisons);

		// cleaup
		parameters.getOutput().close();
	}

}
