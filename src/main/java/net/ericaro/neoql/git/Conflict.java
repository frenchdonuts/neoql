package net.ericaro.neoql.git;

public interface Conflict<T> {

	/** resolve using whatever is the remote change
	 * 
	 */
	public abstract void resolveRemote();

	/** resolve using whatever is the local change
	 * 
	 */
	public abstract void resolveLocal();

}