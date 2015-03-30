package jacusa.pileup.dispatcher.call;

import jacusa.cli.parameters.TwoSampleCallParameters;
import jacusa.pileup.worker.TwoSampleWindowCallWorker;
import jacusa.util.coordinateprovider.CoordinateProvider;

import java.io.IOException;

public class TwoSampleWindowCallWorkerDispatcher extends AbstractCallWorkerDispatcher<TwoSampleWindowCallWorker> {

	private TwoSampleCallParameters parameters;

	public TwoSampleWindowCallWorkerDispatcher(CoordinateProvider coordinateProvider, TwoSampleCallParameters parameters) throws IOException {
		super(	coordinateProvider, 
				parameters.getMaxThreads(), 
				parameters.getOutput(), 
				parameters.getFormat()
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

	/* TODO move to format 
	@Override
	protected String getHeader() {
		int replicates1 = parameters.getSample1().getPathnames().length;
		int replicates2 = parameters.getSample2().getPathnames().length;

		return getFormat().getHeader(new DefaultParallelPileup(replicates1, replicates2));
	}
	*/

}