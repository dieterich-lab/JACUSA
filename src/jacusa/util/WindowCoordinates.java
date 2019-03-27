package jacusa.util;

public class WindowCoordinates {

	private String contig;
	private int genomicWindowStart;

	private int windowSize;
	private int maxGenomicPosition;

	public WindowCoordinates(final String contig, final int genomicWindowStart, final int windowSize, final int maxGenomicPosition) {
		this.contig = contig;
		this.genomicWindowStart = genomicWindowStart;
		this.windowSize = windowSize;
		this.maxGenomicPosition = maxGenomicPosition;
	}

	public String getContig() {
		return contig;
	}

	public void setContig(String contig) {
		this.contig = contig;
	}

	public int getGenomicWindowStart() {
		return genomicWindowStart;
	}

	public void setGenomicWindowStart(int genomicWindowStart) {
		this.genomicWindowStart = genomicWindowStart;
	}

	public int getWindowSize() {
		return windowSize;
	}

	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

	public int getMaxGenomicPosition() {
		return maxGenomicPosition;
	}

	public void setMaxGenomicPosition(int maxGenomicPosition) {
		this.maxGenomicPosition = maxGenomicPosition;
	}
	
	/**
	 * End of window (inclusive)
	 * @return
	 */
	public int getGenomicWindowEnd() {
		return Math.min(genomicWindowStart + windowSize -1, maxGenomicPosition);
	}

	/**
	 * 
	 * @param genomicPosition
	 * @return
	 */
	public boolean isContainedInGenome(int genomicPosition) {
		return genomicPosition <= maxGenomicPosition && genomicPosition > 0;
	}

	/**
	 * 
	 * @param genomicPosition
	 * @return
	 */
	public boolean isContainedInWindow(int genomicPosition) {
		return genomicPosition >= genomicWindowStart && genomicPosition <= getGenomicWindowEnd();
	}

	/**
	 * Calculates genomicPosition 
	 * @param genomicPosition
	 * @return
	 */
	public int convert2WindowPosition(final int genomicPosition) {
		int windowPosition = genomicPosition - genomicWindowStart;
		
		if(genomicPosition > getGenomicWindowEnd()){
			return -1;
		}

		return Math.max(-1, windowPosition);
	}
	
	public int get2WindowPosition(final int genomicPosition) {
		return genomicPosition - genomicWindowStart;
	}

	public int getOrientation(final int genomicPosition) {
		if(genomicPosition < genomicWindowStart) {
			return -1;
		}
		
		if(genomicPosition > getGenomicWindowEnd()){
			return 1;
		}
		
		return 0;
	}
	
	public boolean isUpstream(final int genomicPosition) {
		return genomicPosition < genomicWindowStart;
	}
	
	public boolean isDownstream(final int genomicPosition) {
		return genomicPosition > getGenomicWindowEnd();
	}

	/**
	 * 
	 * @param windowPosition
	 * @return
	 */
	public int getGenomicPosition(int windowPosition) {
		return genomicWindowStart + windowPosition;
	}

}