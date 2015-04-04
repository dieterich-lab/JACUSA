package jacusa.pileup.dispatcher.call;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jacusa.cli.parameters.TwoSampleCallParameters;
import jacusa.pileup.DefaultParallelPileup;
import jacusa.pileup.DefaultPileup;
import jacusa.pileup.DefaultPileup.STRAND;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Pileup;
import jacusa.pileup.worker.TwoSampleDebugCallWorker;
import jacusa.util.Coordinate;
import jacusa.util.coordinateprovider.CoordinateProvider;

public class TwoSampleDebugCallWorkerDispatcher extends AbstractCallWorkerDispatcher<TwoSampleDebugCallWorker> {

	private TwoSampleCallParameters parameters;
	private Map<String, ParallelPileup> coord2parallelPileup; 
	
	public TwoSampleDebugCallWorkerDispatcher(CoordinateProvider coordinateProvider, TwoSampleCallParameters parameters) throws IOException {
		super(	coordinateProvider, 
				parameters.getMaxThreads(), 
				parameters.getOutput(), 
				parameters.getFormat(),
				parameters.isSeparate()
		);
		
		this.parameters = parameters;
		coord2parallelPileup = initCoord2parallelPileup(parameters.getSample1().getPathnames()[0]);
	}

	private Map<String, ParallelPileup> initCoord2parallelPileup(String pathname) {
		Map<String, ParallelPileup> coord2parallelPileup = new HashMap<String, ParallelPileup>();
		
		File file = new File(pathname);
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

				// store parallelPileup at given coordinate
				Coordinate coordinate = new Coordinate(sequenceName, start, end);
				ParallelPileup parallelPileup = new DefaultParallelPileup(pileups1, pileups2);
				parallelPileup.setContig(sequenceName);
				parallelPileup.setStart(start);
				parallelPileup.setEnd(end);
				parallelPileup.setStrand(strand);
				coord2parallelPileup.put(coordinate.toString(), parallelPileup);
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return coord2parallelPileup;
		} catch (IOException e) {
			e.printStackTrace();
			return coord2parallelPileup;
		}

		return coord2parallelPileup;
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
					pileups[i].getCounts().getBaseCount()[j] = count;
					pileups[i].getCounts().getQualCount()[j][qual] = count;
				}
			}
		}
		return pileups;
	}

	public Map<String, ParallelPileup> getCoord2parallelPileup() {
		return coord2parallelPileup;
	}

	@Override
	protected TwoSampleDebugCallWorker buildNextWorker() {
		return new TwoSampleDebugCallWorker(
				this,
				this.getWorkerContainer().size(),
				parameters
		);
	}

}