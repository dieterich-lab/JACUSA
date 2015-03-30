package jacusa.pileup.dispatcher.call;

import jacusa.cli.parameters.TwoSampleCallParameters;
import jacusa.pileup.worker.TwoSampleCallWorker;
import jacusa.util.coordinateprovider.CoordinateProvider;

import java.io.IOException;

public class TwoSampleCallWorkerDispatcher extends AbstractCallWorkerDispatcher<TwoSampleCallWorker> {

	private TwoSampleCallParameters parameters;
	
	public TwoSampleCallWorkerDispatcher(
			final CoordinateProvider coordinateProvider,
			final TwoSampleCallParameters parameters) throws IOException {
		super(coordinateProvider, 
			 parameters.getMaxThreads(),
			 parameters.getOutput(),
			 parameters.getFormat()
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

	/* TODO move to result
	@Override
	protected String getHeader() {
		int replicates1 = parameters.getSample1().getPathnames().length;
		int replicates2 = parameters.getSample2().getPathnames().length;

		return getFormat().getHeader(new DefaultParallelPileup(replicates1, replicates2));
	}
	*/

}