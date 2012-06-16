package net.ericaro.neoql;

public interface Mapping<S, T> {

	/** retrieve existing relation, and remove it
	 * 
	 * @param s
	 * @return
	 */
	public T pop(S s);
	/** create relation, and return it
	 * 
	 * @param s
	 * @return
	 */
	public T push(S s);
	/** retrieve existing relation, but keep it.
	 * 
	 * @param s
	 * @return
	 */
	public T peek(S s);
	
}
