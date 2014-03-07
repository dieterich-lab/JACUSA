package accusa2.process.parallelpileup.dispatcher;

import java.io.File;
import java.io.IOException;

import accusa2.cli.Parameters;
import accusa2.io.output.Output;
import accusa2.io.output.TmpOutputReader;
import accusa2.io.output.TmpOutputWriter;
import accusa2.process.parallelpileup.worker.MpileupParallelPileupWorker;
import accusa2.util.CoordinateProvider;

public class MpileupParallelPileupWorkerDispatcher extends AbstractParallelPileupWorkerDispatcher<MpileupParallelPileupWorker> {

	public MpileupParallelPileupWorkerDispatcher(CoordinateProvider coordinateProvider, Parameters parameters) {
		super(coordinateProvider, parameters);
	}

	@Override
	protected void processFinishedWorker(MpileupParallelPileupWorker processParallelPileup) {
		synchronized (comparisons) {
			comparisons += processParallelPileup.getComparisons();
		}

		// Deprecated
		/*
		synchronized (tmpOutputs) {
			for(int i : processParallelPileup.getTmpOutputWriters().keySet()) {
				tmpOutputs[i] = processParallelPileup.getTmpOutputWriters().get(i);
			}
		}
		*/
	}

	@Override
	protected MpileupParallelPileupWorker buildNextParallelPileupWorker() {
		return new MpileupParallelPileupWorker(this, next(), parameters);
	}

	@Override
	protected void writeOuptut() {
		final Output output = parameters.getOutput();

		TmpOutputWriter[] tmpOutputWriters = null;
		for(final Output tmpOutput : tmpOutputWriters) {
			try {
				tmpOutput.close();

				final TmpOutputReader tmpOutputReader = new TmpOutputReader(tmpOutput.getInfo());
				String line = null;
				while((line = tmpOutputReader.readLine()) != null) {
					output.write(line);
				}
				tmpOutputReader.close();

				new File(tmpOutput.getInfo()).delete();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

}
