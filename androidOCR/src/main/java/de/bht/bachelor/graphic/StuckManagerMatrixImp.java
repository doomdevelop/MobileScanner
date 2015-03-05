package de.bht.bachelor.graphic;

import java.util.LinkedList;
import java.util.List;

import android.graphics.Matrix;

public class StuckManagerMatrixImp implements StuckManager<Matrix> {

	public StuckManagerMatrixImp(int sizeLimit) {
		if (sizeLimit <= 0)
			throw new IllegalArgumentException("The length must be biger than 0 !");
		this.sizeLimit = sizeLimit;
		stack = new LinkedList<Matrix>();
	}

	@Override
	public boolean addLast(Matrix t) {
		if (stack.size() == sizeLimit) {
			stack.removeFirst();
		}
		stack.addLast(t);
		return true;
	}

	@Override
	public boolean remove(Matrix t) {
		return stack.remove(t);
	}

	@Override
	public Matrix getFirst() {
		if (stack.size() == 0)
			return null;
		return stack.removeFirst();
	}

	@Override
	public Matrix getLast() {
		if (stack.size() == 0)
			return null;
		return stack.removeLast();
	}

	@Override
	public Matrix get(int i) {
		return stack.get(i);
	}

	@Override
	public void removeAll() {
		stack.clear();

	}

	/**
	 * 
	 * @return true if stuck is empty
	 */
	public boolean isEmpty() {
		return stack.size() == 0;
	}

	@Override
	public int getSize() {
		return stack.size();
		// TODO Auto-generated method stub
	}

	public List<Matrix> getList() {
		return stack;
	}

	private final int sizeLimit;
	private final LinkedList<Matrix> stack;
}
