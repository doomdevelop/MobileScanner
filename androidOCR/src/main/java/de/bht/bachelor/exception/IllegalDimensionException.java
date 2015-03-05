package de.bht.bachelor.exception;

import de.bht.bachelor.beans.Dimension;

public class IllegalDimensionException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public IllegalDimensionException() {
		super();
	}

	public IllegalDimensionException(Dimension checkedDimension) {
		super();
		this.correctMoveDimension = checkedDimension;
	}

	public IllegalDimensionException(String message) {
		super(message);
	}

	public IllegalDimensionException(String message, int w, int h) {
		super(message);
		this.w = w;
		this.h = h;
	}

	public int getW() {
		return w;
	}

	public int getH() {
		return h;
	}

	public Dimension getCorrectMoveDimension() {
		return correctMoveDimension;
	}

	private int w = -1;
	private int h = -1;
	private Dimension correctMoveDimension;

}
