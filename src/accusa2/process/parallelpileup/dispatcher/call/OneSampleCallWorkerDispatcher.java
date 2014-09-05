package accusa2.process.parallelpileup.dispatcher.call;

import java.io.IOException;

import accusa2.cli.parameters.OneSampleCallParameters;
import accusa2.pileup.DefaultParallelPileup;
import accusa2.process.parallelpileup.worker.OneSampleCallWorker;
import accusa2.util.CoordinateProvider;

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
		return 	getFormat().getHeader(new DefaultParallelPileup(parameters.getSampleA().getPathnames().length, 0));
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