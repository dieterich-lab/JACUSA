package jacusa.pileup.worker;

import jacusa.JACUSA;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Result;
import jacusa.pileup.dispatcher.AbstractWorkerDispatcher;
import jacusa.pileup.iterator.AbstractWindowIterator;
import jacusa.util.Coordinate;
import jacusa.util.Location;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import net.sf.samtools.SAMFileReader;

public abstract class AbstractWorker extends Thread {

	public static enum STATUS {INIT, READY, FINISHED, BUSY, DONE};

	private Coordinate coordinate;
	protected AbstractWindowIterator parallelPileupIterator;

	protected AbstractWorkerDispatcher<? extends AbstractWorker> workerDispatcher;

	private final int threadId;
	protected final int maxThreads;

	private STATUS status;
	protected int comparisons;

	private BufferedWriter bw;
	
	public AbstractWorker(
			AbstractWorkerDispatcher<? extends AbstractWorker> workerDispatcher,
			int threadId, 
			int maxThreads) {
		this.workerDispatcher 	= workerDispatcher;
		this.threadId			= threadId;
		this.maxThreads			= maxThreads;

		status 					= STATUS.INIT;
		comparisons 			= 0;
		
		String filename = workerDispatcher.getOutput().getInfo() + "_" + threadId + "_tmp.gz";
		final File file = new File(filename);
		try {
			bw = new BufferedWriter(new FileWriter(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized void setCoordinate(Coordinate coordinate) {
		this.coordinate = coordinate;
	}
	
	public final void run() {
		while (status != STATUS.FINISHED) {
			switch (status) {

			case READY:
				synchronized (this) {
					
					status = STATUS.BUSY;
					parallelPileupIterator = buildIterator(coordinate);
					processParallelPileupIterator(parallelPileupIterator);
					status = STATUS.INIT;
				}
			break;
				
			case INIT:
				Coordinate coordinate = null;
				synchronized (workerDispatcher) {
					if (workerDispatcher.hasNext()) {
						if (maxThreads > 0 && workerDispatcher.getThreadIds().size() > 0) {
							try {
								bw.write("##\n");
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						workerDispatcher.getThreadIds().add(getThreadId());
						coordinate = workerDispatcher.next();
					}
				}
				synchronized (this) {
					if (coordinate == null) {
						setStatus(STATUS.FINISHED);
					} else {
						setCoordinate(coordinate);
						setStatus(STATUS.READY);
					}
				}
				break;

			
			default:
				break;
			}
		}
		
		synchronized (workerDispatcher) {
			workerDispatcher.notify();
		}
		
		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getThreadId() {
		return threadId;
	}

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
	}

	protected abstract Result processParallelPileup(ParallelPileup parallelPileup, final Location location, final AbstractWindowIterator parallelPileupIterator);
	
	/**
	 * 
	 * @param parallelPileupIterator
	 */
	protected synchronized void processParallelPileupIterator(final AbstractWindowIterator parallelPileupIterator) {
		// print informative log
		JACUSA.printLog("Started screening contig " + 
				parallelPileupIterator.getCoordinate().getSequenceName() + 
				":" + 
				parallelPileupIterator.getCoordinate().getStart() + 
				"-" + 
				parallelPileupIterator.getCoordinate().getEnd());

		// iterate over parallel pileups
		while (parallelPileupIterator.hasNext()) {
			final Location location = parallelPileupIterator.next();
			final ParallelPileup parallelPileup = parallelPileupIterator.getParallelPileup().copy();
			final Result result = processParallelPileup(parallelPileup, location, parallelPileupIterator);

			// considered comparisons

			comparisons++;

			if (result == null) {
				continue;
			}

			final String line = workerDispatcher.getFormat().convert2String(result);
			try {
				bw.write(line + "\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	abstract protected void close();

	/**
	 * 
	 * @param coordinate
	 * @param parameters
	 * @return
	 */
	protected abstract AbstractWindowIterator buildIterator(Coordinate coordinate);
	
	public final int getComparisons() {
		return comparisons;
	}

	public STATUS getStatus() {
		return status;
	}
	
	public int getMaxThreads() {
		return maxThreads;
	}
	
	public void setStatus(STATUS status) {
		this.status = status;
	}
	
}