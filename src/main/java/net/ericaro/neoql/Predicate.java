package net.ericaro.neoql;

/** any operation that evaluate T to a boolean. Implementations should be stateless and immutable.
 * 
 * @author eric
 *
 * @param <T>
 */
public interface Predicate<T> {

	public boolean eval(T t);
	
}
