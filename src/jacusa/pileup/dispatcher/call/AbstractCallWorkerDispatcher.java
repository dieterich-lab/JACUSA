package jacusa.pileup.dispatcher.call;

import jacusa.io.Output;
import jacusa.io.format.AbstractOutputFormat;
import jacusa.pileup.dispatcher.AbstractWorkerDispatcher;
import jacusa.pileup.worker.AbstractCallWorker;
import jacusa.util.coordinateprovider.CoordinateProvider;

import java.io.IOException;

public abstract class AbstractCallWorkerDispatcher<T extends AbstractCallWorker> extends AbstractWorkerDispatcher<T> {
	
	public AbstractCallWorkerDispatcher(
			final int n1,
			final int n2,
			final CoordinateProvider coordinateProvider, 
			final int maxThreads,
			final Output output,
			final AbstractOutputFormat format,
			final boolean separate) throws IOException {
		super(n1, n2, coordinateProvider, maxThreads, output, format, separate);
	}

}