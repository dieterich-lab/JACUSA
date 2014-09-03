package accusa2.process.parallelpileup.worker;

import java.io.File;
import java.io.IOException;

import net.sf.samtools.SAMFileReader;
import accusa2.io.Output;
import accusa2.io.TmpWriter;
import accusa2.io.format.output.AbstractOutputFormat;
import accusa2.pileup.iterator.AbstractParallelPileupWindowIterator;
import accusa2.process.parallelpileup.dispatcher.AbstractWorkerDispatcher;
import accusa2.util.AnnotatedCoordinate;

public abstract class AbstractWorker extends Thread {

	protected AbstractWorkerDispatcher<? extends AbstractWorker> workerDispatcher;
	protected AbstractParallelPileupWindowIterator parallelPileupIterator;

	protected final int maxThreads;
	protected final int threadId;
	protected int nextThreadId;

	protected int comparisons;

	// output related
	// current writer
	protected TmpWriter tmpOutputWriter;
	protected Output output;
	protected AbstractOutputFormat format;

	// indicates if computation is finished
	private boolean isFinished;

	public AbstractWorker(
			AbstractWorkerDispatcher<? extends AbstractWorker> workerDispatcher, 
			int maxThreads, 
			Output output, 
			AbstractOutputFormat format) {
		this.workerDispatcher 	= workerDispatcher; 

		this.maxThreads			= maxThreads;
		isFinished 				= false;
		comparisons 			= 0;

		threadId				= workerDispatcher.getThreadContainer().size();
		nextThreadId			= -1;
		// FIXME parallelPileupIterator  = buildParallelPileupIterator(workerDispatcher.next(this), parameters);

		final String tmpFilename = output.getInfo() + "_tmp" + String.valueOf(threadId) + ".gz";
		try {
			tmpOutputWriter		= new TmpWriter(tmpFilename);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	private synchronized void writeNextThreadID() {
		while(getNextThreadId() == -1) {
			if(getNextThreadId() >= 0) {
				try {
					tmpOutputWriter.write(format.getCOMMENT() + String.valueOf(getNextThreadId()));
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
				tmpOutputWriter.write(format.getCOMMENT() + String.valueOf(getNextThreadId()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
	}

	public final void run() {
		processParallelPileupIterator(parallelPileupIterator);

		if (maxThreads > 1) {
			writeNextThreadID();
		}

		while (! isFinished) {
			AnnotatedCoordinate annotatedCoordinate = null;

			synchronized (workerDispatcher) {
				if(workerDispatcher.hasNext()) {
					annotatedCoordinate = workerDispatcher.next(this);
				} else {
					isFinished = true;
				}
			}

			if (annotatedCoordinate != null) {
				parallelPileupIterator = buildParallelPileupIterator(annotatedCoordinate);
				processParallelPileupIterator(parallelPileupIterator);

				if (maxThreads > 1) {
					writeNextThreadID();
				}
			}
		}

		// this thread is done - tell dispatcher
		synchronized (workerDispatcher) {
			workerDispatcher.notify();
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

	protected abstract void close();

	/**
	 * 
	 * @return
	 */
	protected SAMFileReader[] createBAMFileReaders(String[] pathnames) {
		return initReaders(pathnames);
	}

	/**
	 * 
	 * @param pathnames
	 * @return
	 */
	protected SAMFileReader[] initReaders(String[] pathnames) {
		SAMFileReader[] readers = new SAMFileReader[pathnames.length];
		for(int i = 0; i < pathnames.length; ++i) {
			readers[i] = initReader(pathnames[i]);
		}
		return readers;
	}

	/**
	 * 
	 * @param pathname
	 * @return
	 */
	protected SAMFileReader initReader(final String pathname) {
		SAMFileReader reader = new SAMFileReader(new File(pathname));
		// be silent
		reader.setValidationStringency(SAMFileReader.ValidationStringency.LENIENT);
		// disable memory mapping
		reader.enableIndexMemoryMapping(false);
		return reader;
	}

	protected void close(SAMFileReader[] readers) {
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
	abstract protected AbstractParallelPileupWindowIterator buildParallelPileupIterator(AnnotatedCoordinate coordinate);

	public final int getComparisons() {
		return comparisons;
	}

	public final boolean isFinished() {
		return isFinished;
	}

	public final TmpWriter getTmpOutputWriter() {
		return tmpOutputWriter;
	}

}