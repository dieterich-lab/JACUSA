package jacusa.pileup.dispatcher.pileup;

import jacusa.cli.parameters.TwoSamplePileupParameters;

import jacusa.pileup.dispatcher.AbstractWorkerDispatcher;
import jacusa.pileup.worker.MpileupWorker;
import jacusa.util.coordinateprovider.CoordinateProvider;

public class MpileupWorkerDispatcher extends AbstractWorkerDispatcher<MpileupWorker> {

	private final TwoSamplePileupParameters parameters;
	
	public MpileupWorkerDispatcher(
			final int n1,
			final int n2,
			final CoordinateProvider coordinateProvider, 
			final TwoSamplePileupParameters parameters) {
		super(
				n1,
				n2,
				coordinateProvider, 
				parameters.getMaxThreads(), 
				parameters.getOutput(), 
				parameters.getFormat(),
				parameters.isSeparate()
		);
		this.parameters = parameters;
	}

	@Override
	protected MpileupWorker buildNextWorker() {
		return new MpileupWorker(
				this, 
				this.getWorkerContainer().size(), 
				parameters
		);
	}

}