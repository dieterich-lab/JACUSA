package jacusa.pileup.dispatcher.call;

import jacusa.cli.parameters.OneSampleCallParameters;
import jacusa.pileup.worker.OneSampleCallWorker;
import jacusa.util.coordinateprovider.CoordinateProvider;

import java.io.IOException;

public class OneSampleCallWorkerDispatcher extends AbstractCallWorkerDispatcher<OneSampleCallWorker> {

	private OneSampleCallParameters parameters;
	
	public OneSampleCallWorkerDispatcher(CoordinateProvider coordinateProvider,	OneSampleCallParameters parameters) throws IOException {
		super(coordinateProvider, 
			  parameters.getMaxThreads(),
			  parameters.getOutput(), 
			  parameters.getFormat()
		);
		
		this.parameters = parameters;
	}

	/* TODO move to result
	@Override
	protected String getHeader() {
		return getFormat().getHeader(new DefaultParallelPileup(parameters.getSample1().getPathnames().length, 0));
	}
	*/

	@Override
	protected OneSampleCallWorker buildNextWorker() {
		return new OneSampleCallWorker(
				this, 
				this.getWorkerContainer().size(),
				parameters
		);
	}

}