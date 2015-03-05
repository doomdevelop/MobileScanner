package de.bht.bachelor.helper;

public class Timer {
	public void start() {
		start = System.currentTimeMillis();
		running = true;
	}

	public void stop() {
		runTime = System.currentTimeMillis() - start;
		running = false;
	}

	/**
	 * 
	 * @return true if timer is running
	 */
	public boolean isRunning() {
		return running;
	}

	public long getRunTime() {
		return runTime;
	}

	private long start;
	private boolean running = false;
	private long runTime;

}
