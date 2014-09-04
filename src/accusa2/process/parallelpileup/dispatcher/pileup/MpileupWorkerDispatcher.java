package accusa2.process.parallelpileup.dispatcher.pileup;

import java.io.IOException;

import accusa2.cli.parameters.TwoSamplePileupParameters;
import accusa2.process.parallelpileup.dispatcher.AbstractWorkerDispatcher;
import accusa2.process.parallelpileup.worker.MpileupWorker;
import accusa2.util.CoordinateProvider;

public class MpileupWorkerDispatcher extends AbstractWorkerDispatcher<MpileupWorker> {

	private final TwoSamplePileupParameters parameters;
	
	public MpileupWorkerDispatcher(final CoordinateProvider coordinateProvider, final TwoSamplePileupParameters parameters) {
		super(coordinateProvider, parameters.getMaxThreads(), parameters.getOutput(), parameters.getFormat(), parameters.isDebug());
		this.parameters = parameters;
	}
	
	@Override
	protected void processFinishedWorker(MpileupWorker processParallelPileup) {
		addComparisons(processParallelPileup.getComparisons());
	}

	@Override
	protected MpileupWorker buildNextWorker() {
		return new MpileupWorker(this, parameters);
	}

	@Override
	protected void processTmpLine(String line) throws IOException {
		output.write(line);
	}

	@Override
	protected String getHeader() {
		return null;
	}

}