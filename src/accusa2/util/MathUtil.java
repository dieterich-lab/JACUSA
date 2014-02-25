package accusa2.util;

public abstract class MathUtil {

	/*
	public static double[] rms(double[][] o) {
		int rows = o.length;
		int cols = o[0].length;

		double[] result = new double[cols];

		for(double[] row : o) {

			assert(row.length == cols);

			for(int i = 0; i < cols; ++i) {
				result[i] += row[i] * row[i];
			}
		}

		for(int i = 0; i < cols; ++i) {
			result[i] = java.lang.Math.sqrt(result[i] / (double)rows);
		}

		return result;
	}
	*/

	public static double[] mean(double[][] o) {
		int rows = o.length;
		int cols = o[0].length;

		double[] result = new double[cols];

		for(double[] row : o) {

			assert(row.length == cols);

			for(int i = 0; i < cols; ++i) {
				result[i] += row[i];
			}
		}

		for(int i = 0; i < cols; ++i) {
			result[i] /= (double)rows;
		}

		return result;
	}

	/**
	 * 
	 * @param basqs
	 * @return
	 */
	public static byte mean(byte[] basqs) {
		int sum = 0;
		for(byte basq : basqs) {
			sum += (int)basq;
		}

		return (byte)(sum / basqs.length); 
	}
	
/*	
	public static double[] normalize(double[] o) {
		int n = o.length;
		double[] result = new double[n];
		double sum = 0.0;
		
		for(int i = 0; i < n; ++i) {
			sum += (double)o[i];
		}
		
		for(int i = 0; i < n; ++i) {
			result[i] = (double)o[i] / sum;
		}
		
		return result;
	}
*/
	
	public static double[] sum(double[][] o) {
		int cols = o[0].length;

		double[] result = new double[cols];

		for(double[] row : o) {

			assert(row.length == cols);

			for(int i = 0; i < cols; ++i) {
				result[i] += row[i];
			}
		}

		return result;
	}

}
