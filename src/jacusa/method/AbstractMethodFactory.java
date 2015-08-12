package jacusa.method;

import jacusa.JACUSA;
import jacusa.cli.options.AbstractACOption;
import jacusa.cli.options.sample.MaxDepthSampleOption;
import jacusa.cli.options.sample.MinBASQSampleOption;
import jacusa.cli.options.sample.MinCoverageSampleOption;
import jacusa.cli.options.sample.MinMAPQSampleOption;
import jacusa.cli.options.sample.filter.FilterNHsamTagOption;
import jacusa.cli.options.sample.filter.FilterNMsamTagOption;
import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.pileup.dispatcher.AbstractWorkerDispatcher;
import jacusa.pileup.worker.AbstractWorker;
import jacusa.util.Coordinate;
import jacusa.util.coordinateprovider.CoordinateProvider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMSequenceRecord;

public abstract class AbstractMethodFactory {

	private String name;
	private String desc;

	protected AbstractParameters parameters;
	protected CoordinateProvider coordinateProvider;
	protected Set<AbstractACOption> acOptions;

	public AbstractMethodFactory(String name, String desc) {
		this.name = name;
		this.desc = desc;

		acOptions = new HashSet<AbstractACOption>();
	}

	/**
	 * 
	 * @return
	 */
	public abstract AbstractParameters getParameters();

	/**
	 * 
	 */
	public abstract void initACOptions();

	protected void initSampleACOptions(int sample, SampleParameters sampleParameters) {
		acOptions.add(new MinMAPQSampleOption(sample, sampleParameters));
		acOptions.add(new MinBASQSampleOption(sample, sampleParameters));
		acOptions.add(new MinCoverageSampleOption(sample, sampleParameters));
		acOptions.add(new MaxDepthSampleOption(sample, sampleParameters));
		acOptions.add(new FilterNHsamTagOption(sample, sampleParameters));
		acOptions.add(new FilterNMsamTagOption(sample, sampleParameters));
	}

	/**
	 * 
	 * @param pathnames1
	 * @param pathnames2
	 * @param coordinateProvider
	 * @return
	 * @throws IOException
	 */
	public abstract AbstractWorkerDispatcher<? extends AbstractWorker> getInstance(
			String[] pathnames1, 
			String[] pathnames2, 
			CoordinateProvider coordinateProvider) throws IOException; 

	/**
	 * 
	 * @return
	 */
	public Set<AbstractACOption> getACOptions() {
		return acOptions;
	}

	/**
	 * 
	 * @return
	 */
	public final String getName() {
		return name;
	}

	/**
	 * 
	 * @return
	 */
	public final String getDescription() {
		return desc;
	}
	
	/**
	 * 
	 * @param options
	 */
	public abstract void printUsage();

	/**
	 * 
	 * @throws Exception
	 */
	public abstract void initCoordinateProvider() throws Exception;

	/**
	 * 
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public abstract boolean parseArgs(String[] args) throws Exception;

	/**
	 * 
	 * @param pathnames
	 * @return
	 * @throws Exception
	 */
	protected List<SAMSequenceRecord> getSAMSequenceRecords(String[] pathnames) throws Exception {
		JACUSA.printLog("Computing overlap between sequence records.");

		SAMFileReader reader 			= new SAMFileReader(new File(pathnames[0]));
		List<SAMSequenceRecord> records = reader.getFileHeader().getSequenceDictionary().getSequences();
		// close readers
		reader.close();

		return records;
	}
	
	/**
	 * 
	 * @return
	 */
	public CoordinateProvider getCoordinateProvider() {
		return coordinateProvider;
	}
	
	/**
	 * 
	 * @param pathnames1
	 * @param pathnames2
	 * @return
	 * @throws Exception
	 */
	protected List<SAMSequenceRecord> getSAMSequenceRecords(String[] pathnames1, String[] pathnames2) throws Exception {
		String error = "Sequence Dictionaries of BAM files do not match";

		List<SAMSequenceRecord> records 	= getSAMSequenceRecords(pathnames1);

		List<Coordinate> coordinates = new ArrayList<Coordinate>();
		Set<String> targetSequenceNames = new HashSet<String>();
		for(SAMSequenceRecord record : records) {
			coordinates.add(new Coordinate(record.getSequenceName(), 1, record.getSequenceLength()));
			targetSequenceNames.add(record.getSequenceName());
		}

		if(!isValid(targetSequenceNames, pathnames1) || !isValid(targetSequenceNames, pathnames2)) {
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
	
}
