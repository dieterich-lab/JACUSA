package jacusa.process.parallelpileup.dispatcher.call;

import jacusa.cli.parameters.OneSampleCallParameters;
import jacusa.pileup.DefaultParallelPileup;
import jacusa.process.parallelpileup.worker.OneSampleCallWorker;
import jacusa.util.coordinateprovider.CoordinateProvider;

import java.io.IOException;

public class OneSampleCallWorkerDispatcher extends AbstractCallWorkerDispatcher<OneSampleCallWorker> {

	private OneSampleCallParameters parameters;
	
	public OneSampleCallWorkerDispatcher(CoordinateProvider coordinateProvider,	OneSampleCallParameters parameters) throws IOException {
		super(	coordinateProvider, 
				parameters.getMaxThreads(), 
				parameters.getStatisticParameters(),
				parameters.getOutput(), 
				parameters.getFormat(),
				parameters.isDebug());
		
		this.parameters = parameters;
	}

	@Override
	protected String getHeader() {
		return getFormat().getHeader(new DefaultParallelPileup(parameters.getSample1().getPathnames().length, 0));
	}

	@Override
	protected void processFinishedWorker(OneSampleCallWorker worker) {
		addComparisons(worker.getComparisons());
	}

	@Override
	protected OneSampleCallWorker buildNextWorker() {
		return new OneSampleCallWorker(this, parameters);
	}

}