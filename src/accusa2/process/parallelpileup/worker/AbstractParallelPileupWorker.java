package accusa2.process.parallelpileup.worker;

import java.io.IOException;

import net.sf.samtools.SAMFileReader;
import accusa2.cli.Parameters;
import accusa2.io.format.AbstractResultFormat;
import accusa2.io.output.TmpOutputWriter;
import accusa2.pileup.iterator.AbstractParallelPileupWindowIterator;
import accusa2.process.parallelpileup.dispatcher.AbstractParallelPileupWorkerDispatcher;
import accusa2.util.AnnotatedCoordinate;

public abstract class AbstractParallelPileupWorker extends Thread {

	protected AbstractParallelPileupWorkerDispatcher<? extends AbstractParallelPileupWorker> parallelPileupWorkerDispatcher;

	protected SAMFileReader[] readersA;
	protected SAMFileReader[] readersB;

	protected final Parameters parameters;
	protected AbstractParallelPileupWindowIterator parallelPileupIterator;

	protected final int threadId;
	protected int nextThreadId;
	
	protected int comparisons;

	// output related
	// current writer
	protected TmpOutputWriter tmpOutputWriter;
	// final result format
	protected AbstractResultFormat resultFormat;

	// indicates if computation is finished
	private boolean isFinished;

	public AbstractParallelPileupWorker(AbstractParallelPileupWorkerDispatcher<? extends AbstractParallelPileupWorker> parallelPileupWorkerDispatcher, final Parameters parameters) {
		this.parallelPileupWorkerDispatcher 	= parallelPileupWorkerDispatcher; 

		readersA				= parallelPileupWorkerDispatcher.createBAMFileReaders1();
		readersB				= parallelPileupWorkerDispatcher.createBAMFileReaders2();

		this.parameters 		= parameters;
		resultFormat 			= parameters.getResultFormat();

		isFinished 				= false;
		comparisons 			= 0;

		threadId				= parallelPileupWorkerDispatcher.getThreadContainer().size();
		nextThreadId			= -1;
		parallelPileupIterator  = buildParallelPileupIterator(parallelPileupWorkerDispatcher.next(this), parameters);

		final String tmpFilename = parameters.getOutput().getInfo() + "_tmp" + String.valueOf(threadId) + ".gz";
		try {
			tmpOutputWriter		= new TmpOutputWriter(tmpFilename);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	private synchronized void writeNextThreadID() {
		while(getNextThreadId() == -1) {
			if(getNextThreadId() >= 0) {
				try {
					tmpOutputWriter.write(resultFormat.getCOMMENT() + String.valueOf(getNextThreadId()));
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}

			try {
				wait(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if(getNextThreadId() >= 0) {
			try {
				tmpOutputWriter.write(resultFormat.getCOMMENT() + String.valueOf(getNextThreadId()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
	}

	public final void run() {
		processParallelPileupIterator(parallelPileupIterator);
		if(parameters.getMaxThreads() > 1) {
			writeNextThreadID();
		}

		while(!isFinished) {
			AnnotatedCoordinate annotatedCoordinate = null;

			synchronized (parallelPileupWorkerDispatcher) {
				if(parallelPileupWorkerDispatcher.hasNext()) {
					annotatedCoordinate = parallelPileupWorkerDispatcher.next(this);
				} else {
					isFinished = true;
				}
			}

			if(annotatedCoordinate != null) {
				parallelPileupIterator = buildParallelPileupIterator(annotatedCoordinate, parameters);
				processParallelPileupIterator(parallelPileupIterator);

				if(parameters.getMaxThreads() > 1) {
					writeNextThreadID();
				}
			}
		}

		// this thread is done - tell dispatcher
		synchronized (parallelPileupWorkerDispatcher) {
			parallelPileupWorkerDispatcher.notify();
		}

		close();
	}

	public synchronized int getNextThreadId() {
		return nextThreadId;
	}

	public synchronized void setNextThreadId(int id) {
		nextThreadId = id;
	}

	public int getThreadId() {
		return threadId;
	}

	protected void close() {
		close(readersA);
		close(readersB);
	}

	private void close(SAMFileReader[] readers) {
		for (SAMFileReader reader : readers) {
			if (reader != null) {
				reader.close();
			}
		}

		try {
			tmpOutputWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param parallelPileupIterator
	 */
	abstract protected void processParallelPileupIterator(AbstractParallelPileupWindowIterator parallelPileupIterator);

	/**
	 * 
	 * @param coordinate
	 * @param parameters
	 * @return
	 */
	abstract protected AbstractParallelPileupWindowIterator buildParallelPileupIterator(AnnotatedCoordinate coordinate, Parameters parameters);

	public final int getComparisons() {
		return comparisons;
	}

	public final boolean isFinished() {
		return isFinished;
	}

	public final TmpOutputWriter getTmpOutputWriter() {
		return tmpOutputWriter;
	}

}