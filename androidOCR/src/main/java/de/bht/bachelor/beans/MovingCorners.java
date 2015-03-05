package de.bht.bachelor.beans;

import de.bht.bachelor.ui.Corner;

public class MovingCorners {
	public MovingCorners(Corner parallelCorner, Corner secondCorner, int coordinateOfparallelMovingCorner, Dimension backUpParallelCornerDimension) {
		this.parallelCorner = parallelCorner;
		this.secondCorner = secondCorner;
		this.coordinateOfparallelMovingCorner = coordinateOfparallelMovingCorner;
		this.backUpParallelCornerDimension = backUpParallelCornerDimension;
	}

	/**
	 * 
	 * @return Paralle Corner to the touched corner. This corner will get move together with touched one.
	 */
	public Corner getParallelCorner() {
		return parallelCorner;
	}

	/**
	 * 
	 * @return the Corner on the other side as the parallel.
	 */
	public Corner getSecondCorner() {
		return secondCorner;
	}

	/**
	 * 
	 * @return has 2 values.
	 * @see MovingCorners.X_PARALLEL_MOVING_CORNER
	 * @see MovingCorners.Y_PARALLEL_MOVING_CORNER
	 */
	public int getCoordinateOfparallelMovingCorner() {
		return coordinateOfparallelMovingCorner;
	}

	public Dimension getBackUpParallelCornerDimension() {
		return backUpParallelCornerDimension;
	}

	private final Corner parallelCorner;
	private final Corner secondCorner;
	private final int coordinateOfparallelMovingCorner;
	private final Dimension backUpParallelCornerDimension;

	public static final int Y_PARALLEL_MOVING_CORNER = 0;
	public static final int X_PARALLEL_MOVING_CORNER = 1;
}
