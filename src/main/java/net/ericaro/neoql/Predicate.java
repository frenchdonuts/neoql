package net.ericaro.neoql;

/** any operation that evaluate T to a boolean. Implementations must be stateless and immutable. Meaning that
 * eval(t) should always return the same value for the same t.
 * 
 * 
 * @author eric
 *
 * @param <T>
 */
public interface Predicate<T> {

	public boolean eval(T t);
	
}
