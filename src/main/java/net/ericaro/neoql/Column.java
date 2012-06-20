package net.ericaro.neoql;


public interface Column<T, V> extends Mapper<T, V> {

	/** returns the value of for this column, and src row.
	 * 
	 * @param src
	 * @return
	 */
	V get(T src);
	
	/** Return the column name.
	 * 
	 * @return
	 */
	String getName();
	
	/** returns the class that defines the foreign associated with this column.
	 * returns null if there is no foreign key.
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
	ColumnValue<T, V> set(Singleton<V> value);
	
	 /** Returns a (Column,Value) pair with this column, and <code>value</code> as value.
	  * A ColumnValue is used to update, or insert a row in a table.
	  * 
	  * @param value
	  * @return
	  */
	ColumnValue<T, V> set(V value);
	

	
	Predicate<T> is(final Singleton<V> value);
	
	/** returns a predicate that test the == for this column's value and the value
	 * 
	 * @param value
	 * @return
	 */
	Predicate<T> is(final V value);
	
	/** if this columns has a foreign key, returns a predicate that is true if the pair left joins.
	  * for instance
	  * for a Pair<Student,Teacher> p, and this column is "Student.teacher" then
	  * p.getLeft().teacher = p.getRight()
	  * 
	  * 
	  * @return
	  */
	 Predicate<Pair<T,V>> joins();

	Class<T> getTable();
}
