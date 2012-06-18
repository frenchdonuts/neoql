package net.ericaro.neoql;


public interface Column<T, V> extends Mapper<T, V> {

	/** returns the value of for this column, and src row.
	 * 
	 * @param src
	 * @return
	 */
	V get(T src);
	
	String getName();
	
	Class<V> getForeignTable() ;
	boolean hasForeignKey() ;
	
	ColumnValue<T, V> set(Singleton<V> value);
	ColumnValue<T, V> set(V value);
	
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
