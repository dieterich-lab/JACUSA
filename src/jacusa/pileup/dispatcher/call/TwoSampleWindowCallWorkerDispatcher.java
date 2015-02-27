package jacusa.pileup.dispatcher.call;

import jacusa.cli.parameters.TwoSampleCallParameters;
import jacusa.pileup.DefaultParallelPileup;
import jacusa.pileup.worker.TwoSampleWindowCallWorker;
import jacusa.util.coordinateprovider.CoordinateProvider;

import java.io.IOException;

public class TwoSampleWindowCallWorkerDispatcher extends AbstractCallWorkerDispatcher<TwoSampleWindowCallWorker> {

	private TwoSampleCallParameters parameters;

	public TwoSampleWindowCallWorkerDispatcher(CoordinateProvider coordinateProvider, TwoSampleCallParameters parameters) throws IOException {
		super(	coordinateProvider, 
				parameters.getMaxThreads(), 
				parameters.getStatisticParameters(),
				parameters.getOutput(), 
				parameters.getFormat(),
				parameters.isDebug());

		this.parameters = parameters;
	}

	@Override
	protected TwoSampleWindowCallWorker buildNextWorker() {
		return new TwoSampleWindowCallWorker(this, parameters);
	}

	@Override
	protected void processFinishedWorker(TwoSampleWindowCallWorker worker) {
		addComparisons(worker.getComparisons());
	}

	@Override
	protected String getHeader() {
		int replicates1 = parameters.getSample1().getPathnames().length;
		int replicates2 = parameters.getSample2().getPathnames().length;

		return getFormat().getHeader(new DefaultParallelPileup(replicates1, replicates2));
	}

}