package de.bht.bachelor.graphic;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Matrix;
import android.util.Log;
import de.bht.bachelor.ui.Corner;

public class ImageHistory {
	public ImageHistory(int size) {
		cropMatrix = new StuckManagerMatrixImp(size);
	}

	public void addMatrix(Matrix matrix) {
		Log.d(TAG, "addMatrix current size : " + cropMatrix.getSize());
		cropMatrix.addLast(matrix);
	}

	public Matrix getLastMatrix() {
		Log.d(TAG, "getLastMatrix current size : " + cropMatrix.getSize());
		return cropMatrix.getLast();
	}

	public void addCopyCircles(Corner[] circles) {
		Log.d(TAG, "addCopyCircles current size : " + quadList.size());
		quadList.add(circles);
	}

	public Corner[] getCopyCircles(int i) {
		Log.d(TAG, "getCopyCircles current size : " + quadList.size());
		if (quadList == null || quadList.isEmpty() || i > quadList.size())
			return null;
		return quadList.get(i);
	}

	public boolean isCirclesListEmpty() {
		return quadList.isEmpty();
	}

	public Corner[] getLastCircles() {
		Log.d(TAG, "getLastCircles current size : " + quadList.size());
		Corner[] circles;
		if (quadList.isEmpty())
			return null;
		circles = quadList.get(quadList.size() - 1);
		if (!removeCircles(circles))
			throw new IllegalStateException("Could not remove circles from the History");
		return circles;
	}

	public int getHistorySize() {
		return cropMatrix.getSize();
	}

	public List<Matrix> getMatrixList() {
		return cropMatrix.getList();
	}

	public boolean isMatrixListEmpty() {
		return cropMatrix.isEmpty();
	}

	private boolean removeCircles(Corner[] circles) {
		return quadList.remove(circles);
	}

	public StuckManagerMatrixImp getCropMatrix() {
		return cropMatrix;
	}

	public ArrayList<Corner[]> getQuadList() {
		return quadList;
	}

	public boolean isHistoryEmpty() {
		return quadList.isEmpty() && cropMatrix.isEmpty();
	}

	private static final String TAG = ImageHistory.class.getSimpleName();
	private final StuckManagerMatrixImp cropMatrix;
	private final ArrayList<Corner[]> quadList = new ArrayList<Corner[]>();
}
