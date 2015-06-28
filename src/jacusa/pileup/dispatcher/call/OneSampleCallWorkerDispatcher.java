package jacusa.pileup.dispatcher.call;

import jacusa.cli.parameters.OneSampleCallParameters;
import jacusa.pileup.worker.OneSampleCallWorker;
import jacusa.util.coordinateprovider.CoordinateProvider;

import java.io.IOException;

@Deprecated
public class OneSampleCallWorkerDispatcher extends AbstractCallWorkerDispatcher<OneSampleCallWorker> {

	private OneSampleCallParameters parameters;
	
	public OneSampleCallWorkerDispatcher(int n1, int n2, CoordinateProvider coordinateProvider,	OneSampleCallParameters parameters) throws IOException {
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
	protected OneSampleCallWorker buildNextWorker() {
		return new OneSampleCallWorker(
				this, 
				this.getWorkerContainer().size(),
				parameters
		);
	}

}