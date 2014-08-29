package accusa2.process.parallelpileup.dispatcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.samtools.SAMFileReader;
import accusa2.cli.Parameters;
import accusa2.io.format.ResultFormat;
import accusa2.io.format.TmpResultFormat;
import accusa2.io.output.Output;
import accusa2.io.output.OutputWriter;
import accusa2.io.output.TmpOutputReader;
import accusa2.io.output.TmpOutputWriter;
import accusa2.pileup.DefaultParallelPileup;
import accusa2.process.parallelpileup.worker.AbstractParallelPileupWorker;
import accusa2.util.AnnotatedCoordinate;
import accusa2.util.CoordinateProvider;

public abstract class AbstractParallelPileupWorkerDispatcher<T extends AbstractParallelPileupWorker> {

	protected CoordinateProvider coordinateProvider;
	protected final Parameters parameters;

	protected final List<T> threadContainer;
	protected final List<T> runningThreads;

	protected Integer comparisons;

	protected int lastThreadId;

	public AbstractParallelPileupWorkerDispatcher(CoordinateProvider coordinateProvider, Parameters parameters) {
		this.coordinateProvider = coordinateProvider;
		this.parameters = parameters;

		threadContainer = new ArrayList<T>(parameters.getMaxThreads());
		runningThreads	= new ArrayList<T>(parameters.getMaxThreads());

		comparisons 	= 0;
		lastThreadId	= -1;
	}

	/**
	 * 
	 * @param pathname
	 * @return
	 */
	private SAMFileReader initReader(String pathname) {
		SAMFileReader reader = new SAMFileReader(new File(pathname));
		// be silent
		reader.setValidationStringency(SAMFileReader.ValidationStringency.LENIENT);
		// disable memory mapping
		reader.enableIndexMemoryMapping(false);
		return reader;
	}

	public synchronized AnnotatedCoordinate next(AbstractParallelPileupWorker abstractParallelPileupWorker) {
		int threadId = abstractParallelPileupWorker.getThreadId();

		if(lastThreadId >= 0) {
			threadContainer.get(lastThreadId).setNextThreadId(threadId);
		}
		lastThreadId = threadId;
		AnnotatedCoordinate annotatedCoordinate = coordinateProvider.next();

		// reset
		if(coordinateProvider.hasNext()) {
			abstractParallelPileupWorker.setNextThreadId(-1);
		} else {
			abstractParallelPileupWorker.setNextThreadId(-2);
		}

		return annotatedCoordinate;
	}

	public synchronized boolean hasNext() {
		return coordinateProvider.hasNext();
	}
	
	public final int run() {
		synchronized (this) {

			while(hasNext() || !threadContainer.isEmpty()) {

				// clean finished threads
				for(int i = 0; i < runningThreads.size(); ++i) {
					T processParallelPileupThread = runningThreads.get(i);

					if(processParallelPileupThread.isFinished()) {
						comparisons += processParallelPileupThread.getComparisons();
						processFinishedWorker(processParallelPileupThread);
						runningThreads.remove(processParallelPileupThread);
					}
				}

				// fill thread container
				while(runningThreads.size() < parameters.getMaxThreads() && hasNext()) {
					T processParallelPileupThread = buildNextParallelPileupWorker();
					threadContainer.add(processParallelPileupThread);
					runningThreads.add(processParallelPileupThread);

					processParallelPileupThread.start();
				}

				// computation finished
				if(!hasNext() && runningThreads.isEmpty()) {
					break;
				}

				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		// finally write the output and cleanup
		writeOuptut();
		return comparisons;
	}

	protected abstract void processFinishedWorker(final T processParallelPileup);
	protected abstract T buildNextParallelPileupWorker();	
	protected abstract void processTmpLine(final ResultFormat resultFormat, final Output output, final Output filtered, final String line) throws IOException;
	
	protected void writeOuptut() {
		// write tmp file
		final Output output = parameters.getOutput();
		Output filtered;
		try {
			filtered = new OutputWriter(parameters.getOutput().getInfo() + ".filtered");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		final ResultFormat resultFormat = new TmpResultFormat(parameters.getResultFormat());

		// write Header
		try {
			output.write(resultFormat.getHeader(new DefaultParallelPileup(parameters.getReplicatesA(), parameters.getReplicatesB())));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		// build reader array
		TmpOutputReader[] tmpOutputReaders = new TmpOutputReader[threadContainer.size()];
		for (int i = 0; i < threadContainer.size(); ++i) {
			final TmpOutputWriter tmpOutputWriter = threadContainer.get(i).getTmpOutputWriter();
			TmpOutputReader tmpOutputReader;
			try {
				tmpOutputReader = new TmpOutputReader(tmpOutputWriter.getInfo());
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			tmpOutputReaders[i] = tmpOutputReader;
		}

		// read data and change readers based on meta info/nextThreadId on the fly to reconstruct order of output
		TmpOutputReader tmpOutputReader = tmpOutputReaders[0];
		try {
			String line = null;
			while ((line = tmpOutputReader.readLine()) != null) {
				if (line.charAt(0) == resultFormat.getCOMMENT()) {
					int nextThreadId = Integer.parseInt(line.substring(1));
					tmpOutputReader = tmpOutputReaders[nextThreadId];
				} else {
					processTmpLine(resultFormat, output, filtered, line);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		for(int i = 0; i < threadContainer.size(); ++i) {
			try {
				tmpOutputReaders[i].close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// leave tmp files if needed
			if(!parameters.getDebug()) {
				new File(threadContainer.get(i).getTmpOutputWriter().getInfo()).delete();
			}
		}

		try {
			output.close();
			filtered.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public SAMFileReader[] createBAMFileReaders1() {
		return initReaders(parameters.getPathnames1());
	}

	/**
	 * 
	 * @return
	 */
	public SAMFileReader[] createBAMFileReaders2() {
		return initReaders(parameters.getPathnames2());
	}

	/**
	 * 
	 * @param pathnames
	 * @return
	 */
	private SAMFileReader[] initReaders(String[] pathnames) {
		SAMFileReader[] readers = new SAMFileReader[pathnames.length];
		for(int i = 0; i < pathnames.length; ++i) {
			readers[i] = initReader(pathnames[i]);
		}
		return readers;
	}

	/**
	 * 
	 * @return
	 */

	public List<T> getThreadContainer() {
		return threadContainer;
	}

}
