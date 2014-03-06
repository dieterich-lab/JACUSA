package accusa2.process.parallelpileup.worker;

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

	protected int threadId;
	protected int nextThreadId;
	
	protected int comparisons;

	// output related
	// current writer
	protected TmpOutputWriter tmpOutputWriter;
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

		// FIXME tmpFilename 			= parameters.getOutput() + "_" + String.valueOf(1); // TODO
		buildParallelPileupIterator(coordinate, parameters);

		isFinished 				= false;

		threadId				= -1;
		nextThreadId			= -1;
		
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

	public int getNextThreadId() {
		return nextThreadId;
	}
	
	public void setNextThreadId(int id) {
		nextThreadId = id;
	}

	public int getThreadId() {
		return threadId;
	}

	public void setThreadId(int id) {
		threadId = id;
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
		// TODO

		// let implementing class build the iterator
		parallelPileupIterator = buildParallelPileupIterator_Helper(annotatedCoordinate, parameters);
	}

	public final int getComparisons() {
		return comparisons;
	}

	public final boolean isFinished() {
		return isFinished;
	}

}
