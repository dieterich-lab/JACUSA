package accusa2.process.parallelpileup.dispatcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import accusa2.io.Output;
import accusa2.io.OutputWriter;
import accusa2.io.TmpOutputReader;
import accusa2.io.TmpWriter;
import accusa2.io.format.output.AbstractOutputFormat;
import accusa2.process.parallelpileup.worker.AbstractWorker;
import accusa2.util.AnnotatedCoordinate;
import accusa2.util.CoordinateProvider;

public abstract class AbstractWorkerDispatcher<T extends AbstractWorker> {

	protected CoordinateProvider coordinateProvider;

	protected int maxThreads;
	protected final List<T> workerContainer;
	protected final List<T> runningWorkers;
	protected int lastThreadId;
	
	protected Integer comparisons;

	protected Output output;
	protected AbstractOutputFormat format;
	protected boolean isDebug;

	public AbstractWorkerDispatcher(
			final CoordinateProvider coordinateProvider, 
			final int maxThreads, 
			final Output output, 
			final AbstractOutputFormat format, 
			final boolean isDebug) {
		this.coordinateProvider = coordinateProvider;

		this.maxThreads = maxThreads;
		workerContainer = new ArrayList<T>(maxThreads);
		runningWorkers	= new ArrayList<T>(maxThreads);
		lastThreadId	= -1;
		
		comparisons 	= 0;
		
		this.output		= output;
		this.format		= format;
	}

	protected abstract void processFinishedWorker(final T processParallelPileup);
	protected abstract T buildNextWorker();	
	protected abstract void processTmpLine(final String line) throws IOException;
	protected abstract String getHeader();
	
	public synchronized AnnotatedCoordinate next(final AbstractWorker worker) {
		final int threadId = worker.getThreadId();

		if (lastThreadId >= 0) {
			workerContainer.get(lastThreadId).setNextThreadId(threadId);
		}
		lastThreadId = threadId;
		AnnotatedCoordinate annotatedCoordinate = coordinateProvider.next();

		// reset
		if (coordinateProvider.hasNext()) {
			worker.setNextThreadId(-1);
		} else {
			worker.setNextThreadId(-2);
		}

		return annotatedCoordinate;
	}

	public synchronized boolean hasNext() {
		return coordinateProvider.hasNext();
	}
	
	public final int run() {
		synchronized (this) {

			while (hasNext() || !workerContainer.isEmpty()) {

				// clean finished threads
				for (int i = 0; i < runningWorkers.size(); ++i) {
					T processParallelPileupThread = runningWorkers.get(i);

					if(processParallelPileupThread.isFinished()) {
						comparisons += processParallelPileupThread.getComparisons();
						processFinishedWorker(processParallelPileupThread);
						runningWorkers.remove(processParallelPileupThread);
					}
				}

				// fill thread container
				while (runningWorkers.size() < maxThreads && hasNext()) {
					T worker = buildNextWorker();
					workerContainer.add(worker);
					runningWorkers.add(worker);

					worker.start();
				}

				// computation finished
				if (! hasNext() && runningWorkers.isEmpty()) {
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

	/**
	 * 
	 * @return
	 */

	public List<T> getThreadContainer() {
		return workerContainer;
	}

	public void addComparisons(int comparisons) {
		this.comparisons += comparisons;
	}

	protected void writeOuptut() {
		// write tmp file
		Output filtered;
		try {
			filtered = new OutputWriter(output.getInfo() + ".filtered");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		// write Header
		try {
			String header = getHeader();
			if (header != null) {
				output.write(getHeader());
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		// build reader array
		TmpOutputReader[] tmpOutputReaders = new TmpOutputReader[getThreadContainer().size()];
		for (int i = 0; i < getThreadContainer().size(); ++i) {
			final TmpWriter tmpOutputWriter = getThreadContainer().get(i).getTmpOutputWriter();
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
				if (line.charAt(0) == format.getCOMMENT()) {
					int nextThreadId = Integer.parseInt(line.substring(1));
					tmpOutputReader = tmpOutputReaders[nextThreadId];
				} else {
					processTmpLine(line);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		for (int i = 0; i < getThreadContainer().size(); ++i) {
			try {
				tmpOutputReaders[i].close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// leave tmp files if needed
			if (! isDebug) {
				new File(getThreadContainer().get(i).getTmpOutputWriter().getInfo()).delete();
			}
		}

		try {
			output.close();
			filtered.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
