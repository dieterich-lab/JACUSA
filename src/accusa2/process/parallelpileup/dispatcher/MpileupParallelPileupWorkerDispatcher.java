package accusa2.process.parallelpileup.dispatcher;

import java.io.IOException;

import accusa2.cli.Parameters;
import accusa2.io.format.ResultFormat;
import accusa2.io.output.Output;
import accusa2.process.parallelpileup.worker.MpileupParallelPileupWorker;
import accusa2.util.CoordinateProvider;

public class MpileupParallelPileupWorkerDispatcher extends AbstractParallelPileupWorkerDispatcher<MpileupParallelPileupWorker> {

	public MpileupParallelPileupWorkerDispatcher(CoordinateProvider coordinateProvider, Parameters parameters) {
		super(coordinateProvider, parameters);
	}

	@Override
	protected void processFinishedWorker(MpileupParallelPileupWorker processParallelPileup) {
		comparisons += processParallelPileup.getComparisons();
	}

	@Override
	protected MpileupParallelPileupWorker buildNextParallelPileupWorker() {
		return new MpileupParallelPileupWorker(this, parameters);
	}

	@Override
	protected void processTmpLine(ResultFormat resultFormat, Output output,	Output filtered, String line) throws IOException {
		output.write(line);
	}

}