package jacusa.pileup.dispatcher.call;

import jacusa.cli.parameters.TwoSampleCallParameters;
import jacusa.pileup.worker.TwoSampleWindowCallWorker;
import jacusa.util.coordinateprovider.CoordinateProvider;

import java.io.IOException;

public class TwoSampleWindowCallWorkerDispatcher extends AbstractCallWorkerDispatcher<TwoSampleWindowCallWorker> {

	private TwoSampleCallParameters parameters;

	public TwoSampleWindowCallWorkerDispatcher(int n1, int n2, CoordinateProvider coordinateProvider, TwoSampleCallParameters parameters) throws IOException {
		super(	n1,
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
	protected TwoSampleWindowCallWorker buildNextWorker() {
		return new TwoSampleWindowCallWorker(
				this,
				this.getWorkerContainer().size(),
				parameters
		);
	}

}