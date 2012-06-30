package net.ericaro.neoql;


/** Provides full access to a Table Class attributes.
 * ContentTable require to have full access to a TableClass attribute, even if the object's public interface 
 * must forbid any editing.
 * 
 * 
 * @author eric
 *
 * @param <T> table type
 * @param <V> column value
 */
public interface Attribute<T,V> {

	/**
	 * 
	 * @return the attribute actual type.
	 */
	public Class<V> getType();
	
	/** return the attribute's value for a row
	 * 
	 * @param row
	 * @return the actual value for the row's attribute.
	 */
	public V get(T row);
	
	/** set the attributes's value for the row 
	 * 
	 * @param row
	 * @param value
	 */
	public void set(T row, V value);
	
}
