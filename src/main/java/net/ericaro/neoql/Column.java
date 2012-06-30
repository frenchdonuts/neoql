package net.ericaro.neoql;


/**
 * 
 * @author eric
 *
 * @param <T> Table
 * @param <V> Value
 */
public interface Column<T, V>{


	/** maps a T into an V
	 * 
	 * @param source
	 * @return
	 */
	public V map(T source);

	
	/** returns the value of for this column, and src row.
	 * 
	 * @param src
	 * @return
	 */
	V get(T src);
	
	
	/** returns the class that defines the type associated with this column.
	 * 
	 * @return
	 */
	Class<V> getForeignTable() ;
	
	/** Convenient method to test if there is a foreign key associated with this column
	 * 
	 * @return
	 */
	boolean hasForeignKey() ;
	
	 /** uses the singleton value and then call the set(T value) method. 
	 * @param value
	 * @return
	 */
	ColumnSetter<T, V> set(Singleton<V> value);
	
	 /** Returns a (Column,Value) pair with this column, and <code>value</code> as value.
	  * A ColumnValue is used to update, or insert a row in a table.
	  * 
	  * @param value
	  * @return
	  */
	ColumnSetter<T, V> set(V value);
	

	
	Predicate<T> is(final Singleton<V> value);
	
	/** returns a predicate that test the == for this column's value and the value
	 * 
	 * @param value
	 * @return
	 */
	Predicate<T> is(final V value);
	

	Class<T> getTable();
}
