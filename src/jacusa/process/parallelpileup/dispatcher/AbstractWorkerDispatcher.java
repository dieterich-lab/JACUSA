package jacusa.process.parallelpileup.dispatcher;

import jacusa.io.Output;
import jacusa.io.TmpOutputReader;
import jacusa.io.TmpWriter;
import jacusa.io.format.output.AbstractOutputFormat;
import jacusa.process.parallelpileup.worker.AbstractWorker;
import jacusa.util.Coordinate;
import jacusa.util.coordinateprovider.CoordinateProvider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public abstract class AbstractWorkerDispatcher<T extends AbstractWorker> {

	private CoordinateProvider coordinateProvider;

	private int maxThreads;
	private final List<T> workerContainer;
	private final List<T> runningWorkers;
	
	private Integer comparisons;

	private Output output;
	private AbstractOutputFormat format;
	private boolean isDebug;

	private Vector<Integer> threadIDs;

	public AbstractWorkerDispatcher(
			final CoordinateProvider coordinateProvider, 
			final int maxThreads, 
			final Output output, 
			final AbstractOutputFormat format, 
			final boolean isDebug) {
		this.coordinateProvider = coordinateProvider;

		threadIDs = new Vector<Integer>(5000);

		this.maxThreads = maxThreads;
		workerContainer = new ArrayList<T>(maxThreads);
		runningWorkers	= new ArrayList<T>(maxThreads);

		comparisons 	= 0;

		this.output		= output;
		this.format		= format;
	}

	protected abstract void processFinishedWorker(final T processParallelPileup);
	protected abstract T buildNextWorker();	
	protected abstract void processTmpLine(final String line) throws IOException;
	protected abstract String getHeader();
	
	public synchronized Coordinate next(final AbstractWorker worker) {
		final int threadId = worker.getThreadId();

		threadIDs.add(threadId);
		Coordinate coordinate = coordinateProvider.next();

		return coordinate;
	}

	public synchronized boolean hasNext() {
		return coordinateProvider.hasNext();
	}
	
	public final int run() {
		synchronized (this) {

			while (hasNext() || ! workerContainer.isEmpty()) {

				// clean finished threads
				for (int i = 0; i < runningWorkers.size(); ++i) {
					T runningWorker = runningWorkers.get(i);

					if (runningWorker.isFinished()) {
						comparisons += runningWorker.getComparisons();
						processFinishedWorker(runningWorker);
						runningWorkers.remove(runningWorker);
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

	public List<T> getWorkerContainer() {
		return workerContainer;
	}

	public void addComparisons(int comparisons) {
		this.comparisons += comparisons;
	}

	protected void writeOuptut() {
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
		TmpOutputReader[] tmpOutputReaders = new TmpOutputReader[getWorkerContainer().size()];
		for (int i = 0; i < getWorkerContainer().size(); ++i) {
			final TmpWriter tmpOutputWriter = getWorkerContainer().get(i).getTmpOutputWriter();
			try {
				tmpOutputWriter.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
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

		int threadI = 0;
		TmpOutputReader tmpOutputReader = tmpOutputReaders[threadI];
		try {
			String line = null;
			while ((line = tmpOutputReader.readLine()) != null) {
				if (line.length() > 1 && 
						line.charAt(0) == format.getCOMMENT() && 
						line.charAt(1) == format.getCOMMENT()) {
					threadI++;
					if (threadI < threadIDs.size()) {
						tmpOutputReader = tmpOutputReaders[threadIDs.get(threadI)];
					}
				} else {
					processTmpLine(line);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		for (int i = 0; i < getWorkerContainer().size(); ++i) {
			try {
				tmpOutputReaders[i].close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// leave tmp files if needed
			if (! isDebug) {
				new File(getWorkerContainer().get(i).getTmpOutputWriter().getInfo()).delete();
			}
		}

		try {
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected Output getOutput() {
		return output;
	}
	
}