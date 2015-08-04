package jacusa.pileup.dispatcher.call;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jacusa.cli.parameters.TwoSampleCallParameters;
import jacusa.io.Output;
import jacusa.io.format.AbstractOutputFormat;
import jacusa.method.call.statistic.StatisticCalculator;
import jacusa.pileup.DefaultParallelPileup;
import jacusa.pileup.DefaultPileup;
import jacusa.pileup.Result;
import jacusa.pileup.DefaultPileup.STRAND;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Pileup;
import jacusa.pileup.worker.AbstractCallWorker;
import jacusa.util.coordinateprovider.CoordinateProvider;

public class TwoSampleDebugCallWorkerDispatcher extends AbstractCallWorkerDispatcher<AbstractCallWorker> {

	private TwoSampleCallParameters parameters;

	public TwoSampleDebugCallWorkerDispatcher(String[] pathnames1, String[] pathnames2, CoordinateProvider coordinateProvider, TwoSampleCallParameters parameters) throws IOException {
		super(	pathnames1,
				pathnames2,
				coordinateProvider, 
				parameters.getMaxThreads(), 
				parameters.getOutput(), 
				parameters.getFormat(),
				parameters.isSeparate()
		);

		this.parameters = parameters;
	}

	public int run() {
		int comparisons = 0;

		final StatisticCalculator sc = parameters.getStatisticParameters().getStatisticCalculator();
		final Output output = parameters.getOutput();
		final AbstractOutputFormat format = parameters.getFormat();

		File file = new File(parameters.getSample1().getPathnames()[0]);
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			// how is the file structered
			String[] header = {"#contig", "start", "end", "name", "stat", "strand", "bases12", "bases21"};
			List<Integer> replicateIndex1 = new ArrayList<Integer>();
			replicateIndex1.add(6);

			List<Integer> replicateIndex2 = new ArrayList<Integer>();
			replicateIndex2.add(7);
			
			// parsing
			String line;
			while((line = br.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("#")) {
					// read header and adjust replicate indexes
					header = line.split("\t");
					replicateIndex1.clear();
					replicateIndex2.clear();

					for (int i = 0; i < header.length ; ++i) {
						String e = header[i];
						if (e.startsWith("bases1")) {
							replicateIndex1.add(i);
						}
						if (e.startsWith("bases2")) {
							replicateIndex2.add(i);
						}
					}
					continue;
				}
				if (line.isEmpty()) {
					continue;
				}

				String[] cols = line.split("\t");
				String sequenceName = cols[0];
				int start = Integer.parseInt(cols[1]);
				int end = Integer.parseInt(cols[2]);
				// ignore those values
				// String name = cols[3];
				// String stat = cols[4];
				STRAND strand = STRAND.UNKNOWN;

				Pileup[] pileups1 = getPileup(sequenceName, end, strand, replicateIndex1, cols);
				Pileup[] pileups2 = getPileup(sequenceName, end, strand, replicateIndex2, cols);

				ParallelPileup parallelPileup = new DefaultParallelPileup(pileups1, pileups2);
				parallelPileup.setContig(sequenceName);
				parallelPileup.setStart(start);
				parallelPileup.setEnd(end);
				parallelPileup.setStrand(strand);

				Result result = new Result();
				result.setParellelPileup(parallelPileup);

				sc.addStatistic(result);

				output.write(format.convert2String(result));
			}
			br.close();
			output.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return comparisons; 
	}

	private Pileup[] getPileup(String sequenceName, int end, STRAND strand, List<Integer> replicateIndex, String cols[]) {
		byte qual = 40;
		Pileup[] pileups = new DefaultPileup[replicateIndex.size()];
		for (int i = 0; i < replicateIndex.size(); ++i) {
			pileups[i] = new DefaultPileup(sequenceName, end, strand, 4);
			String[] bases = cols[replicateIndex.get(i)].split(",");
			for (int j = 0; j < bases.length; ++j) {
				int count = Integer.parseInt(bases[j]);
				if (count > 0) {
					pileups[i].getCounts().setBaseCount(j, count);
					pileups[i].getCounts().getQualCount(j)[qual] = count;
				}
			}
		}
		return pileups;
	}

	@Override
	protected AbstractCallWorker buildNextWorker() {
		// not needed
		return null;
	}

}