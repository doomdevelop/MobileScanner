package de.bht.bachelor.graphic;

import java.util.LinkedList;

import android.graphics.Bitmap;

/**
 * This is implementation of stack with limit of elements.
 * Every new element is add to the and of the list (addLast(t))
 * If the stuck is full first element (removeFirst()) will be remove and the new one will be add as last.
 * 
 * Src for LinkedList vs ArrayList
 * http://leepoint.net/notes-java/algorithms/big-oh/bigoh.html
 * 
 * @author and
 * 
 */
public class StuckManagerImp implements StuckManager<Bitmap> {

	public StuckManagerImp(int sizeLimit) {
		if (sizeLimit <= 0)
			throw new IllegalArgumentException("The length must be bigger than 0 !");
		this.sizeLimit = sizeLimit;
		stack = new LinkedList<Bitmap>();
	}

	@Override
	public boolean addLast(Bitmap t) {
		if (stack.size() == sizeLimit) {
			stack.removeFirst();
		}
		stack.addLast(t);
		return true;
	}

	@Override
	public boolean remove(Bitmap t) {
		return stack.remove(t);
	}

	/**
	 * Remove and return the last element from stuck
	 */
	@Override
	public Bitmap getLast() {
		if (stack.size() == 0)
			return null;
		return stack.removeLast();
	}

	/**
	 * Remove and return the first element from stuck
	 */
	@Override
	public Bitmap getFirst() {
		if (stack.size() == 0)
			return null;
		return stack.removeFirst();
	}

	@Override
	public Bitmap get(int i) {
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

	private final int sizeLimit;
	private final LinkedList<Bitmap> stack;

}
