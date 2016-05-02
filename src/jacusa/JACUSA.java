/*
    JAVA framework for accurate SNV assessment (JACUSA) is a one-stop solution to detect single
nucleotide variants (SNVs) from comparing matched sequencing samples.
    Copyright (C) 2015  Michael Piechotta

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jacusa;



import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.CLI;
import jacusa.cli.parameters.hasSample2;
import jacusa.method.AbstractMethodFactory;
// import jacusa.method.call.OneSampleCallFactory;
// import jacusa.method.call.OneSampleCallFactory;
import jacusa.method.call.TwoSampleCallFactory;
//import jacusa.method.call.TwoSampleDebugCallFactory;
import jacusa.method.pileup.TwoSamplePileupFactory;
import jacusa.pileup.dispatcher.AbstractWorkerDispatcher;
import jacusa.pileup.worker.AbstractWorker;
import jacusa.util.Coordinate;
import jacusa.util.SimpleTimer;
import jacusa.util.coordinateprovider.BedCoordinateProvider;
import jacusa.util.coordinateprovider.CoordinateProvider;
import jacusa.util.coordinateprovider.ThreadedCoordinateProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMSequenceRecord;

/**
 * @author Michael Piechotta
 */
public class JACUSA {

	// timer used for all time measurements
	private static SimpleTimer timer;
	public static final String NAME = "jacusa.jar";
	public static final String VERSION = "1.0.0";

	// command line interface
	private CLI cli;

	/**
	 * 
	 */
	public JACUSA() {
		cli = CLI.getSingleton();

		// container for available methods (e.g.: call, pileup)
		Map<String, AbstractMethodFactory> methodFactories = new TreeMap<String, AbstractMethodFactory>();

		AbstractMethodFactory[] factories = new AbstractMethodFactory[] {
			// new OneSampleCallFactory(), 
			new TwoSampleCallFactory(),
			new TwoSamplePileupFactory(),
			// RC new TwoSampleWindowCallFactory(),

			// new TwoSampleDebugCallFactory()
		};
		for (AbstractMethodFactory factory : factories) {
			methodFactories.put(factory.getName(), factory);
		}

		// add to cli 
		cli.setMethodFactories(methodFactories);
	}

	/**
	 * Singleton Pattern
	 * @return a SimpleTimer instance
	 */
	public static SimpleTimer getSimpleTimer() {
		if (timer == null) {
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

		List<Coordinate> coordinates = new ArrayList<Coordinate>();
		Set<String> targetSequenceNames = new HashSet<String>();
		for (SAMSequenceRecord record : records) {
			coordinates.add(new Coordinate(record.getSequenceName(), 1, record.getSequenceLength()));
			targetSequenceNames.add(record.getSequenceName());
		}

		if (! isValid(targetSequenceNames, pathnames1) || !isValid(targetSequenceNames, pathnames2)) {
			throw new Exception(error);
		}

		return records;
	}

	/**
	 * 
	 * @param targetSequenceNames
	 * @param pathnames
	 * @return
	 */
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
	public void printProlog(String[] args) {
		String lineSep = "--------------------------------------------------------------------------------";

		System.err.println(lineSep);

		StringBuilder sb = new StringBuilder();
		sb.append(NAME);
		sb.append(" Version: ");
		sb.append(VERSION);
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
		String time = "[ INFO ] " + getSimpleTimer().getTotalTimestring() + "\t:\t";
		System.err.println(time + " " + line);
	}

	public static void printWarning(String line) {
		String time = "[ WARNING ] " + getSimpleTimer().getTotalTimestring() + "\t:\t";
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
		JACUSA jacusa = new JACUSA();
		CLI cmd = jacusa.getCLI();
		// parse CLI
		if (! cmd.processArgs(args)) {
			System.exit(1);
		}

		// instantiate chosen method
		AbstractMethodFactory methodFactory = cmd.getMethodFactory();
		AbstractParameters parameters = methodFactory.getParameters();

		// process coordinate provider
		CoordinateProvider coordinateProvider = null;
		if (parameters.getBedPathname().isEmpty()) {
			methodFactory.initCoordinateProvider();
			coordinateProvider = methodFactory.getCoordinateProvider();
		} else {
			coordinateProvider = new BedCoordinateProvider(parameters.getBedPathname());
		}

		// prolog
		jacusa.printProlog(args);
		String[] pathnames1 = parameters.getSample1().getPathnames();
		String[] pathnames2 = new String[0];
		if (parameters instanceof hasSample2) {
			pathnames2 = ((hasSample2)parameters).getSample2().getPathnames();
		}

		// wrap chosen coordinate provider 
		if (parameters.getMaxThreads() > 1) {
			coordinateProvider = new ThreadedCoordinateProvider(coordinateProvider, pathnames1, pathnames2, parameters.getThreadWindowSize());
		}

		// main
		AbstractWorkerDispatcher<? extends AbstractWorker> workerDispatcher = methodFactory.getInstance(pathnames1, pathnames2, coordinateProvider);
		int comparisons = workerDispatcher.run();

		// epilog
		jacusa.printEpilog(comparisons);

		// cleaup
		parameters.getOutput().close();
	}

}
