package net.ericaro.neoql;


/** The Data Manipulation Language interface
 * 
 * @author eric
 *
 */
public interface DML {

	<T> T insert(ContentTable<T> table, ColumnSetter<T, ?>... values);

	<T> T insert(ContentTable<T> table, T t);
	
	<T> void update(ContentTable<T> table, Predicate<T> where, ColumnSetter<T, ?>... setters);

	<T> void update(ContentTable<T> table, Predicate<T> where, T t);
		
	<T> void delete(ContentTable<T> table, Predicate<T> where);

}
