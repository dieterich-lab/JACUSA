package jacusa.process.parallelpileup.dispatcher.pileup;

import jacusa.cli.parameters.TwoSamplePileupParameters;
import jacusa.process.parallelpileup.dispatcher.AbstractWorkerDispatcher;
import jacusa.process.parallelpileup.worker.MpileupWorker;
import jacusa.util.coordinateprovider.CoordinateProvider;

import java.io.IOException;

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
		getOutput().write(line);
	}

	@Override
	protected String getHeader() {
		return null;
	}

}