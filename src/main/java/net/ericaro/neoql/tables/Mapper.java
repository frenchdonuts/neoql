package net.ericaro.neoql.tables;

public interface Mapper<S, T> {

	/** maps a T into an S
	 * 
	 * @param source
	 * @return
	 */
	public T map(S source);
	
}
