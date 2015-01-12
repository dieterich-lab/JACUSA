package jacusa.cli.options.sample.filter.samtag;

import net.sf.samtools.SAMRecord;

public interface SamTagFilter {

	boolean filter(SAMRecord samRecord);

}
