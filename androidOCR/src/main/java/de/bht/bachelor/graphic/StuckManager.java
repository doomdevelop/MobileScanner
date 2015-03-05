package de.bht.bachelor.graphic;

/**
 * Generic Interface for managering stuck
 * 
 * @author and
 * 
 * @param <T>
 */
public interface StuckManager<T> {
	/**
	 * add to the end of the stuck (as last)
	 * 
	 * @param t
	 * @return false if could not add Element t to the stuck
	 */
	public boolean addLast(T t);

	/**
	 * Remove specific element
	 * 
	 * @param t
	 *            element
	 * @return false if there was no element t in the stuck
	 */
	public boolean remove(T t);

	/**
	 * Remove and return the first elemnt from the stuck
	 * 
	 * @return
	 */
	public T getFirst();

	/**
	 * 
	 * @return last added element to the stuck
	 */
	public T getLast();

	/**
	 * Return element from stuck on the specific position
	 * 
	 * @param i
	 * @return
	 */
	public T get(int i);

	/**
	 * remove all elemnts from the stuck
	 * 
	 * @return
	 */
	public void removeAll();

	public int getSize();

}
