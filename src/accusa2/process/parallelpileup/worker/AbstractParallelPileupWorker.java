package accusa2.process.parallelpileup.worker;



import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import net.sf.samtools.SAMFileReader;

import accusa2.cli.Parameters;
import accusa2.io.format.AbstractResultFormat;
import accusa2.io.output.TmpOutputWriter;
import accusa2.method.statistic.StatisticCalculator;
import accusa2.pileup.iterator.ParallelPileupIterator;
import accusa2.process.parallelpileup.dispatcher.AbstractParallelPileupWorkerDispatcher;
import accusa2.util.AnnotatedCoordinate;

public abstract class AbstractParallelPileupWorker extends Thread {

	protected AbstractParallelPileupWorkerDispatcher<? extends AbstractParallelPileupWorker> parallelPileupWorkerDispatcher;

	protected SAMFileReader[] readers1;
	protected SAMFileReader[] readers2;

	protected final Parameters parameters;
	protected ParallelPileupIterator parallelPileupIterator;

	protected StatisticCalculator statistic;

	protected int comparisons;

	// output related
	// current writer
	protected TmpOutputWriter tmpOutput;
	// all writers
	protected Map<Integer, TmpOutputWriter> tmpOutputWriters;
	// final result format
	protected AbstractResultFormat resultFormat;

	// indicates if computation is finished
	private boolean isFinished;

	public AbstractParallelPileupWorker(AbstractParallelPileupWorkerDispatcher<? extends AbstractParallelPileupWorker> parallelPileupWorkerDispatcher, final AnnotatedCoordinate coordinate, final Parameters parameters) {
		this.parallelPileupWorkerDispatcher 	= parallelPileupWorkerDispatcher; 

		readers1				= parallelPileupWorkerDispatcher.createBAMFileReaders1();
		readers2				= parallelPileupWorkerDispatcher.createBAMFileReaders2();

		this.parameters 		= parameters;
		resultFormat 			= parameters.getResultFormat();

		tmpOutputWriters 		= new TreeMap<Integer, TmpOutputWriter>();
		buildParallelPileupIterator(coordinate, parameters);

		isFinished 				= false;
		
		comparisons 			= 0;
	}

	public final void run() {
		processParallelPileupIterator(parallelPileupIterator);

		while(!isFinished) {
			AnnotatedCoordinate annotatedCoordinate = null;
			synchronized (parallelPileupWorkerDispatcher) {
				if(parallelPileupWorkerDispatcher.hasNext()) {
					annotatedCoordinate = parallelPileupWorkerDispatcher.next();
				} else {
					isFinished = true;
				}
			}

			if(annotatedCoordinate != null) {
				buildParallelPileupIterator(annotatedCoordinate, parameters);
				processParallelPileupIterator(parallelPileupIterator);
			}

			synchronized (parallelPileupWorkerDispatcher) {
				parallelPileupWorkerDispatcher.notify();
			}
		}
		close();

	}

	protected void close() {
		close(readers1);
		close(readers2);
	}

	private void close(SAMFileReader[] readers) {
		for(SAMFileReader reader : readers) {
			if(reader != null) {
				reader.close();
			}
		}
	}
	
	/**
	 * 
	 * @param parallelPileupIterator
	 */
	abstract protected void processParallelPileupIterator(ParallelPileupIterator parallelPileupIterator);
	
	/**
	 * 
	 * @param coordinate
	 * @param parameters
	 * @return
	 */
	abstract protected ParallelPileupIterator buildParallelPileupIterator_Helper(AnnotatedCoordinate coordinate, Parameters parameters);

	/**
	 * 
	 * @param annotatedCoordinate
	 * @param parameters
	 */
	private final void buildParallelPileupIterator(AnnotatedCoordinate annotatedCoordinate, Parameters parameters) {
		try {
			// close existing tmp file
			if(tmpOutputWriters.size() > 0) {
				getCurrentTmpOutputWriter().close();
			}

			if(tmpOutputWriters.containsKey(annotatedCoordinate.getIndex())) {
				throw new IllegalArgumentException("Duplicate coorindateIndex: " + annotatedCoordinate.getIndex());
			}
			tmpOutputWriters.put(
					annotatedCoordinate.getIndex(), 
					new TmpOutputWriter(
							File.createTempFile(
									"ACCUSA2_" + annotatedCoordinate.getSequenceName() + "_" + String.valueOf(annotatedCoordinate.getIndex()), 
									null)
							)
					);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// let implementing class build the iterator
		parallelPileupIterator = buildParallelPileupIterator_Helper(annotatedCoordinate, parameters);
		tmpOutput = getCurrentTmpOutputWriter();
	}

	public final int getComparisons() {
		return comparisons;
	}

	public final boolean isFinished() {
		return isFinished;
	}

	protected final int getAnnotatedCoordinateIndex() {
		return parallelPileupIterator.getAnnotatedCoordinate().getIndex();
	}

	protected final TmpOutputWriter getCurrentTmpOutputWriter() {
		return getTmpOutputWriters().get(getAnnotatedCoordinateIndex());
	}

	public final Map<Integer, TmpOutputWriter> getTmpOutputWriters() {
		return tmpOutputWriters;
	}

}
