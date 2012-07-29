package net.ericaro.neoql;


/** The Data Manipulation Language interface
 * 
 * @author eric
 *
 */
public interface DML {

	<T> T insert(ContentTable<T> table, ColumnSetter<T, ?>... values);
	
	<T> void update(ContentTable<T> table, Predicate<T> where, ColumnSetter<T, ?>... setters);
	
	<T> void delete(ContentTable<T> table, Predicate<T> where);

}
