package jacusa.pileup.dispatcher.call;

import jacusa.cli.parameters.TwoSampleCallParameters;
import jacusa.pileup.worker.TwoSampleCallWorker;
import jacusa.util.coordinateprovider.CoordinateProvider;

import java.io.IOException;

public class TwoSampleCallWorkerDispatcher extends AbstractCallWorkerDispatcher<TwoSampleCallWorker> {

	private TwoSampleCallParameters parameters;
	
	public TwoSampleCallWorkerDispatcher(
			final int n1,
			final int n2,
			final CoordinateProvider coordinateProvider,
			final TwoSampleCallParameters parameters) throws IOException {
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
	protected TwoSampleCallWorker buildNextWorker() {
		return new TwoSampleCallWorker(
				this, 
				this.getWorkerContainer().size(),
				parameters);
	}

}