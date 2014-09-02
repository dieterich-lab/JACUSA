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
import accusa2.cli.parameters.CLI;
import accusa2.method.TwoSampleCallFactory;
import accusa2.method.AbstractMethodFactory;
import accusa2.method.TwoSamplePileupFactory;
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
	public static final String VERSION = "2.7";
	
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
		methodFactory = new TwoSampleCallFactory();
		methodFactories.put(methodFactory.getName(), methodFactory);

		methodFactory = new TwoSamplePileupFactory();
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
	 * @param pathnamesA
	 * @param pathnamesB
	 * @return
	 * @throws Exception
	 */
	public List<SAMSequenceRecord> getSAMSequenceRecords(String[] pathnamesA, String[] pathnamesB) throws Exception {
		printLog("Computing overlap between sequence records.");
		String error = "Sequence Dictionary of BAM files do not match";

		SAMFileReader reader 				= new SAMFileReader(new File(pathnamesA[0]));
		List<SAMSequenceRecord> records 	= reader.getFileHeader().getSequenceDictionary().getSequences();
		// close readers
		reader.close();

		List<AnnotatedCoordinate> coordinates = new ArrayList<AnnotatedCoordinate>();
		Set<String> targetSequenceNames = new HashSet<String>();
		for (SAMSequenceRecord record : records) {
			coordinates.add(new AnnotatedCoordinate(record.getSequenceName(), 1, record.getSequenceLength()));
			targetSequenceNames.add(record.getSequenceName());
		}

		if (! isValid(targetSequenceNames, pathnamesA) || !isValid(targetSequenceNames, pathnamesB)) {
			throw new Exception(error);
		}

		return records;
	}

	private boolean isValid(Set<String> targetSequenceNames, String[] pathnames) {
		Set<String> sequenceNames = new HashSet<String>();
		for (String pathname : pathnames) {
			SAMFileReader reader = new SAMFileReader(new File(pathname));
			List<SAMSequenceRecord> records	= reader.getFileHeader().getSequenceDictionary().getSequences();
			for (SAMSequenceRecord record : records) {
				sequenceNames.add(record.getSequenceName());
			}	
			reader.close();
		}
		
		if (! sequenceNames.containsAll(targetSequenceNames) || !targetSequenceNames.containsAll(sequenceNames)) {
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
		String time = "[ INFO ] " + getSimpleTimer().getTotalTimestring() + ": ";
		System.err.println(time + " " + line);
	}

	/**
	 * 
	 * @param comparisons
	 */
	public void printEpilog(int comparisons) {
		// print statistics to STDERR
		printLog("Screening done using " + cli.getMethodFactory().getParameters().getMaxThreads() + " thread(s)");

		System.err.println("Results can be found in: " + cli.getMethodFactory().getParameters().getOutput().getInfo());

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

		if (! cmd.processArgs(args)) {
			System.exit(1);
		}
		// FIXME
		Parameters parameters = cmd.getParameters();

		String[] pathnamesA = parameters.getPathnamesA();
		String[] pathnamesB = parameters.getPathnamesB();
		List<SAMSequenceRecord> records = accusa2.getSAMSequenceRecords(pathnamesA, pathnamesB);
		CoordinateProvider coordinateProvider = new SAMCoordinateProvider(records);

		if (! parameters.getBED_Pathname().isEmpty()) {
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