package de.bht.bachelor.graphic.transform;

import de.bht.bachelor.beans.Dimension;

public class Vector {
	public Vector(Dimension startPoint, Dimension endPoint) {
		this.startPoint = startPoint;
		this.endPoint = endPoint;
	}

	/**
	 * Calculating translation values for x , y
	 * The way of calculating is :
	 * 
	 * float x = endPoint.getX() - startPoint.getX();
	 * float y = endPoint.getY() - startPoint.getY();
	 * 
	 * @return Dimension with translated values x / y.
	 */
	public Dimension getTranslation() {
		float x = endPoint.getX() - startPoint.getX();
		float y = endPoint.getY() - startPoint.getY();
		return new Dimension(x, y);
	}

	/**
	 * 
	 * @return
	 */
	public Dimension getStartPoint() {
		return startPoint;
	}

	public Dimension getEndPoint() {
		return endPoint;
	}

	public void ignoreX() {
		endPoint.setX(startPoint.getX());
	}

	public void ignoreY() {
		endPoint.setY(startPoint.getY());
	}

	@Override
	public Vector clone() {
		return new Vector(new Dimension(startPoint), new Dimension(endPoint));
	}

	@Override
	public boolean equals(Object vector) {
		if (vector == null)
			return false;
		if (!(vector instanceof Vector))
			return false;
		return ((Vector) vector).getStartPoint().equals(this.startPoint) && ((Vector) vector).getEndPoint().equals(this.endPoint);
	}

	private final Dimension startPoint;
	private final Dimension endPoint;
}
