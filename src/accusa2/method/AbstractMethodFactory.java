package accusa2.method;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMSequenceRecord;
import accusa2.ACCUSA;
import accusa2.cli.options.AbstractACOption;
import accusa2.cli.parameters.AbstractParameters;
import accusa2.process.parallelpileup.dispatcher.AbstractWorkerDispatcher;
import accusa2.process.parallelpileup.worker.AbstractWorker;
import accusa2.util.AnnotatedCoordinate;
import accusa2.util.CoordinateProvider;

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

	public abstract AbstractParameters getParameters();

	public abstract void initACOptions();
	public abstract AbstractWorkerDispatcher<? extends AbstractWorker> getInstance(CoordinateProvider coordinateProvider) throws IOException; 

	
	public Set<AbstractACOption> getACOptions() {
		return acOptions;
	}

	public final String getName() {
		return name;
	}

	public final String getDescription() {
		return desc;
	}
	
	public abstract void initCoordinateProvider() throws Exception;
	
	protected List<SAMSequenceRecord> getSAMSequenceRecords(String[] pathnames) throws Exception {
		ACCUSA.printLog("Computing overlap between sequence records.");

		SAMFileReader reader 			= new SAMFileReader(new File(pathnames[0]));
		List<SAMSequenceRecord> records = reader.getFileHeader().getSequenceDictionary().getSequences();
		// close readers
		reader.close();

		return records;
	}

	public CoordinateProvider getCoordinateProvider() {
		return coordinateProvider;
	}

	/**
	 * 
	 * @param pathnamesA
	 * @param pathnamesB
	 * @return
	 * @throws Exception
	 */
	protected List<SAMSequenceRecord> getSAMSequenceRecords(String[] pathnamesA, String[] pathnamesB) throws Exception {
		String error = "Sequence Dictionary of BAM files do not match";

		List<SAMSequenceRecord> records 	= getSAMSequenceRecords(pathnamesA);

		List<AnnotatedCoordinate> coordinates = new ArrayList<AnnotatedCoordinate>();
		Set<String> targetSequenceNames = new HashSet<String>();
		for(SAMSequenceRecord record : records) {
			coordinates.add(new AnnotatedCoordinate(record.getSequenceName(), 1, record.getSequenceLength()));
			targetSequenceNames.add(record.getSequenceName());
		}

		if(!isValid(targetSequenceNames, pathnamesA) || !isValid(targetSequenceNames, pathnamesB)) {
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
	

}