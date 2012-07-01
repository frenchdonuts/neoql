package net.ericaro.neoql;


/**
 * 
 * @author eric
 *
 * @param <T> Table type
 * @param <C> Column Type
 */
public interface Column<T, C>{
	
	/** returns the value of for this column, and src row.
	 * 
	 * @param src
	 * @return
	 */
	C get(T src);
	
	
	/** returns the class that defines the type associated with this column.
	 * 
	 * @return
	 */
	Class<C> getType() ;
	
	/** Convenient method to test if there is a foreign key associated with this column
	 * 
	 * @return
	 */
	boolean hasForeignKey() ;
	
	 /**Convenient method. equivalent to:
	  * 	<code>set(singleton.get() )</code> 
	  * uses the singleton value and then call the set(T value) method. 
	 * @param singleton
	 * @return
	 */
	ColumnSetter<T, C> set(Singleton<C> singleton);
	
	 /** Returns a ColumnSetter object dedicated to set <code>value</code> on this column.
	  * A ColumnSetter is used to update, or insert a row in a table.
	  * 
	  * @param value
	  * @return
	  */
	ColumnSetter<T, C> set(C value);
	
	/** return a predicate that test the identity of the given singleton
	 * 
	 * @param value
	 * @return
	 */
	Predicate<T> is(final Singleton<C> value);
	
	/** returns a predicate that test the == for this column's value and the value
	 * 
	 * @param value
	 * @return
	 */
	Predicate<T> is(final C value);
	
	/** return the actual table type this column is attached to.
	 * 
	 * @return
	 */
	Class<T> getTable();
}
