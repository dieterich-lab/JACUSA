package accusa2.util;

import java.text.NumberFormat;

/**
 * Implements a simple timer for benchmarking purposes.
 * 
 * @author Sebastian Fröhler
 *
 */
public class SimpleTimer {

	private long time;
	private long totalTime;
	private NumberFormat format;
	
	public SimpleTimer(){
		this.startTimer();
		this.format = NumberFormat.getInstance();
	}
	
	public void startTimer(){
		time = System.currentTimeMillis();
		totalTime = System.currentTimeMillis();
	}
	
	public void resetTimer(){
		this.startTimer();
	}
	
	public synchronized long getTime(){
		long currentTime = (System.currentTimeMillis() - time);
		time = System.currentTimeMillis();
		return currentTime;
	}
	
	public String getTimeString(){
		long currentTime = this.getTime();
		return format.format(currentTime) + "ms";
	}
	
	public long getTotalTime(){
		return (System.currentTimeMillis() - totalTime);
	}

	public String getTotalMinTimestring(){
		return format.format(getTotalTime() / (1000 * 60)) + "min";
	}

	public String getTotalTimestring(){
		long totalTime = getTotalTime();
		
		int sec = 1000;
		int min = sec * 60;
		int hour = min * 60;

		if(2 * sec - totalTime > 0) {
			return format.format(getTotalTime()) + "msec";
		} else if(min - totalTime > 0) {
			return format.format(getTotalTime() / sec) + "sec";
		} else if(hour - totalTime > 0) {
			return format.format(getTotalTime() / min) + "min";			
		} else {
			return format.format(getTotalTime() / hour) + "h";
		}
	}
	
}
