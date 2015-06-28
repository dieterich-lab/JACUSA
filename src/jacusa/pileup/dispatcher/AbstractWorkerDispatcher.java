package jacusa.pileup.dispatcher;

import jacusa.io.Output;
import jacusa.io.OutputWriter;
import jacusa.io.format.AbstractOutputFormat;
import jacusa.pileup.worker.AbstractWorker;
import jacusa.util.Coordinate;
import jacusa.util.coordinateprovider.CoordinateProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public abstract class AbstractWorkerDispatcher<T extends AbstractWorker> {

	private final CoordinateProvider coordinateProvider;
	private final int maxThreads;
	private final Output output;

	private final AbstractOutputFormat format;
	private final boolean separate;

	private final List<T> workerContainer;
	private final List<T> runningWorkers;

	private Integer comparisons;
	private List<Integer> threadIds;
	
	private String[] pathnames1;
	private String[] pathnames2;
	
	public AbstractWorkerDispatcher(
			final String[] pathnames1,
			final String[] pathnames2,
			final CoordinateProvider coordinateProvider, 
			final int maxThreads, 
			final Output output, 
			final AbstractOutputFormat format,
			final boolean separate) {
		this.pathnames1 = pathnames1;
		this.pathnames2 = pathnames2;
		
		this.coordinateProvider = coordinateProvider;
		this.maxThreads 		= maxThreads;
		this.output 			= output;
		this.format				= format;
		this.separate			= separate;
		
		workerContainer 		= new ArrayList<T>(maxThreads);
		runningWorkers			= new ArrayList<T>(maxThreads);
		comparisons 			= 0;
		threadIds				= new ArrayList<Integer>(10000);
	}

	protected abstract T buildNextWorker();	

	public synchronized Coordinate next() {
		return coordinateProvider.next();
	}

	public synchronized boolean hasNext() {
		return coordinateProvider.hasNext();
	}

	public final int run() {
		// write Header
		try {
			String header = format.getHeader(pathnames1, pathnames2);
			if (header != null) {
				output.write(header);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		while (hasNext() || ! runningWorkers.isEmpty()) {
			for (int i = 0; i < runningWorkers.size(); ++i) {
				T runningWorker = runningWorkers.get(i);
				
				switch (runningWorker.getStatus()) {
				case FINISHED:
					synchronized (comparisons) {
						comparisons += runningWorker.getComparisons();
					}
					synchronized (runningWorkers) {
						runningWorkers.remove(runningWorker);
					}
					break;

				default:
					break;
				}
			} 

			synchronized (this) {
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
					this.wait(60 * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		writeOutput();

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
	
	public AbstractOutputFormat getFormat() {
		return format;
	}

	public Output getOutput() {
		return output;
	}

	public List<Integer> getThreadIds() {
		return threadIds;
	}
	
	protected void writeOutput() {
		Output filteredOutput = null;
		if (separate) {
			final String filename = output.getInfo().concat(".filtered");
			final File file = new File(filename);
			try {
				filteredOutput = new OutputWriter(file);
				filteredOutput.write(format.getHeader(pathnames1, pathnames2));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		BufferedReader[] brs = new BufferedReader[maxThreads];
		for (int threadId = 0; threadId < maxThreads; ++threadId) {
			String filename = output.getInfo() + "_" + threadId + "_tmp.gz";
			final File file = new File(filename); 
			try {
				brs[threadId] = new BufferedReader(
						new InputStreamReader(
								new GZIPInputStream(
										new FileInputStream(file), 10000)));
			} catch (IOException e) {
					e.printStackTrace();
			}
		}

		for (int threadId : threadIds) {
			final BufferedReader br = brs[threadId];
			try {
				String line;
				while((line = br.readLine()) != null && ! line.startsWith("##")) {
					final int i = line.length() - 1;
					final char c = line.charAt(i);
					if (separate == false || c == 'F') {
						output.write(line.substring(0, i));
					} else {
						filteredOutput.write(line.substring(0, i));
					}
					
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		for (BufferedReader br : brs) {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		for (int threadId = 0; threadId < maxThreads; ++threadId) {
			String filename = output.getInfo() + "_" + threadId + "_tmp.gz";
			new File(filename).delete();
		}
		
		try {
			output.close();
			if (filteredOutput != null) {
				filteredOutput.close();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}