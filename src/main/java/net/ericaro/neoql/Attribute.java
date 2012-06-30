package net.ericaro.neoql;


/** Interface to provide full access to a Table class attributes.
 * 
 * 
 * @author eric
 *
 * @param <T> table type
 * @param <V> column value
 */
public interface Attribute<T,V> {

	
	public String getName();
	public Class<V> getType();
	/** return the attribute's value for object
	 * 
	 * @param object
	 * @return
	 */
	public V get(T object);
	/** set the attributes's value for object 
	 * 
	 * @param object
	 * @param value
	 */
	public void set(T object, V value);
}
