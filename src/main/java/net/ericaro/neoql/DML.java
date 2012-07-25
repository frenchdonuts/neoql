package net.ericaro.neoql;

import net.ericaro.neoql.properties.SingletonProperty;

/** The Data Manipulation Language interface
 * 
 * @author eric
 *
 */
public interface DML {

	<T> T insert(ContentTable<T> table, ColumnSetter<T, ?>... values);
	
	<T> void update(ContentTable<T> table, Predicate<T> where, ColumnSetter<T, ?>... setters);
	
	//<T> T update(SingletonProperty<T> property, T value);
	
	/** move the row pointer to another row identified by a row value
	 * 
	 * @param cursor
	 * @param value
	 */
	<T> void moveTo(Cursor<T> cursor, T value);
	
	<T> void delete(ContentTable<T> table, Predicate<T> where);

}
