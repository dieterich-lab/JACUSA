package jacusa.pileup.worker;

import jacusa.io.Output;
import jacusa.io.TmpWriter;
import jacusa.io.format.output.AbstractOutputFormat;
import jacusa.pileup.dispatcher.AbstractWorkerDispatcher;
import jacusa.pileup.iterator.AbstractWindowIterator;
import jacusa.util.Coordinate;

import java.io.File;
import java.io.IOException;

import net.sf.samtools.SAMFileReader;

public abstract class AbstractWorker extends Thread {

	protected AbstractWorkerDispatcher<? extends AbstractWorker> workerDispatcher;
	protected AbstractWindowIterator parallelPileupIterator;

	protected final int maxThreads;
	protected final int threadId;

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

		threadId				= workerDispatcher.getWorkerContainer().size();

		final String tmpFilename = output.getInfo() + "_tmp" + String.valueOf(threadId) + ".gz";
		try {
			tmpOutputWriter		= new TmpWriter(tmpFilename);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		this.output				= output;
		this.format				= format;
		
	}

	private synchronized void writeMarker() {
		StringBuilder sb = new StringBuilder();
		sb.append(format.getCOMMENT());
		sb.append(format.getCOMMENT());
		String s = sb.toString();

		try {
			tmpOutputWriter.write(s);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public final void run() {
		processParallelPileupIterator(parallelPileupIterator);

		if (maxThreads > 1) {
			writeMarker();
		}

		while (! isFinished) {
			Coordinate annotatedCoordinate = null;

			synchronized (workerDispatcher) {
				if (workerDispatcher.hasNext()) {
					annotatedCoordinate = workerDispatcher.next(this);
				} else {
					isFinished = true;
				}
			}

			if (annotatedCoordinate != null) {
				parallelPileupIterator = buildIterator(annotatedCoordinate);
				processParallelPileupIterator(parallelPileupIterator);

				if (maxThreads > 1) {
					writeMarker();
				}
			}
		}

		// this thread is done - tell dispatcher
		synchronized (workerDispatcher) {
			workerDispatcher.notify();
		}

		close();
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
	abstract protected void processParallelPileupIterator(AbstractWindowIterator parallelPileupIterator);

	/**
	 * 
	 * @param coordinate
	 * @param parameters
	 * @return
	 */
	abstract protected AbstractWindowIterator buildIterator(Coordinate coordinate);

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