package jacusa.pileup.dispatcher.call;

import jacusa.cli.parameters.TwoSampleCallParameters;
import jacusa.pileup.DefaultParallelPileup;
import jacusa.pileup.worker.TwoSampleCallWorker;
import jacusa.util.coordinateprovider.CoordinateProvider;

import java.io.IOException;

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
	protected TwoSampleCallWorker buildNextWorker() {
		return new TwoSampleCallWorker(this, parameters);
	}

	@Override
	protected void processFinishedWorker(TwoSampleCallWorker worker) {
		addComparisons(worker.getComparisons());
	}

	@Override
	protected String getHeader() {
		int replicates1 = parameters.getSample1().getPathnames().length;
		int replicates2 = parameters.getSample2().getPathnames().length;

		return getFormat().getHeader(new DefaultParallelPileup(replicates1, replicates2));
	}

}