package de.bht.bachelor.beans;

import java.io.Serializable;

import android.graphics.Point;

public class Dimension implements Serializable {

	public Dimension(float x, float y) {
		this.X = x;
		this.Y = y;
	}

	public Dimension(Point point) {
		this(point.x, point.y);
	}

	public Dimension(Dimension dimension) {
		this(dimension.getX(), dimension.getY());
	}

	public float getX() {
		return X;
	}

	public void setX(float x) {
		X = x;
	}

	public float getY() {
		return Y;
	}

	public void setY(float y) {
		Y = y;
	}

	public float[] toArray() {
		float[] arr = new float[2];
		arr[0] = X;
		arr[1] = Y;
		return arr;
	}

	@Override
	public boolean equals(Object dimension) {
		if (dimension == null)
			return false;
		if (!(dimension instanceof Dimension))
			return false;
		return X == ((Dimension) dimension).getX() && Y == ((Dimension) dimension).getY();
	}

	private float X;
	private float Y;
}
