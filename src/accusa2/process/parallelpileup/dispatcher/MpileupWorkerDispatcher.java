package accusa2.process.parallelpileup.dispatcher;

import java.io.IOException;

import accusa2.cli.parameters.Parameters;
import accusa2.io.Output;
import accusa2.io.format.output.AbstractOutputFormat;
import accusa2.process.parallelpileup.worker.MpileupWorker;
import accusa2.util.CoordinateProvider;

public class MpileupWorkerDispatcher extends AbstractParallelPileupWorkerDispatcher<MpileupWorker> {

	public MpileupWorkerDispatcher(CoordinateProvider coordinateProvider, Parameters parameters) {
		super(coordinateProvider, parameters);
	}

	@Override
	protected void processFinishedWorker(MpileupWorker processParallelPileup) {
		comparisons += processParallelPileup.getComparisons();
	}

	@Override
	protected MpileupWorker buildNextParallelPileupWorker() {
		return new MpileupWorker(this, parameters);
	}

	@Override
	protected void processTmpLine(AbstractOutputFormat outputFormat, Output output,	Output filtered, String line) throws IOException {
		output.write(line);
	}

}