package accusa2.process.parallelpileup.dispatcher.call;

import java.io.IOException;

import accusa2.cli.parameters.TwoSampleCallParameters;
import accusa2.pileup.DefaultParallelPileup;
import accusa2.process.parallelpileup.worker.TwoSampleCallWorker;
import accusa2.util.CoordinateProvider;

public class TwoSampleCallWorkerDispatcher extends AbstractCallWorkerDispatcher<TwoSampleCallWorker> {

	private TwoSampleCallParameters parameters;
	
	public TwoSampleCallWorkerDispatcher(CoordinateProvider coordinateProvider,	TwoSampleCallParameters parameters) throws IOException {
		super(	coordinateProvider, 
				parameters.getMaxThreads(), 
				parameters.getStatisticParameters(),
				parameters.getOutput(), 
				parameters.getFormat(),
				parameters.isDebug());
		
		this.parameters = parameters;
	}

	@Override
	protected TwoSampleCallWorker buildNextParallelPileupWorker() {
		return new TwoSampleCallWorker(this, parameters);
	}

	@Override
	protected void processFinishedWorker(TwoSampleCallWorker callWorker) {
		// nothing to be done
	}

	@Override
	protected String getHeader() {
		int replicatesA = parameters.getSampleA().getPathnames().length;
		int replicatesB = parameters.getSampleB().getPathnames().length;

		return 	getFormat().getHeader(new DefaultParallelPileup(replicatesA, replicatesB));
	}

}