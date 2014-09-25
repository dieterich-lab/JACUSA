package accusa2.util;

import java.util.Arrays;

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

	public static double[] weightedMean(double[] w, double[][] o) {
		int rows = w.length;
		int cols = o[0].length;

		double[] result = new double[cols];

		for(double[] row : o) {

			assert(row.length == cols);

			for(int i = 0; i < cols; ++i) {
				result[i] += w[i] * row[i];
			}
		}

		for(int i = 0; i < cols; ++i) {
			result[i] /= (double)rows;
		}

		return result;
	}

	public static double[] variance(double m[], double[][] o) {
		int rows = o.length;
		int cols = o[0].length;
		
		double[] var = new double[m.length];
		Arrays.fill(var, 0.0);
		if (rows == 1 ) {
			return var;
		}
	
		for(double[] row : o) {

			assert(row.length == cols);

			for(int i = 0; i < cols; ++i) {
				var[i] += Math.pow(m[i] - row[i], 2.0);
			}
		}
		for (int i = 0; i < cols; i++) {
			var[i] /= (rows - 1);
		}

		return var;
	}

	public static double[] weightedVariance(double[] w, double m[], double[][] o) {
		int rows = o.length;
		int cols = o[0].length;
		
		double V1 = 0.0;
		double V2 = 0.0;
		
				
		double[] var = new double[m.length];
		Arrays.fill(var, 0.0);
		if (rows == 1 ) {
			return var;
		}
	
		for (int j = 0; j < rows; ++j) {
			double[] row = o[j];
			assert(row.length == cols);

			for (int i = 0; i < cols; ++i) {
				var[i] += w[j] * Math.pow(m[i] - row[i], 2.0);
			}
			V1 += w[j];
			V2 += w[j] * w[j];
		}
		double V = V1 - (V2 / V1);
		for (int i = 0; i < cols; i++) {
			var[i] /= V;
		}

		return var;
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

	public static double sum(double[] values) {
		double sum = 0.0;
		for (double value : values) {
			sum += value;
		}
		return sum;
	}
	
}
