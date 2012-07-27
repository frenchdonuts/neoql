package net.ericaro.neoql;

/**
 * the Data Definition Language interface.
 * 
 * There are merely only four objects: <ol>
 * <li>Table : a collection of objects</li>
 * <li>Row Property : an observable row of a table. The value changes when the row is updated, and changes are made observable</li>
 * <li>Column Property: a specific column of a given Row Property.</li>
 * <li>Singleton Property: not related to any table, holds a single value, notify of any changes on this value.</li>
 * </ol>
 * 
 * All object can be created and dropped. Every object has a key to identify it, a couple (Class,String) for every property, the class for tables.  
 * 
 * @author eric
 * 
 */
public interface DDL {

	//TODO : finalize the drop protocol (and test it), this requires the capacity to 'idify cursors. Tables are by "class", what about cursor ?
	
	<T> void dropTable(Class<T> table);
	<T> ContentTable<T> createTable(Class<T> table, Column<T, ?>... columns);
	/** creates a new cursor, it delegates the key creation to the database
	 * 
	 * @param table
	 * @return
	 */
	<T> Cursor<T> createCursor(Class<T> table);
	
	<T> Cursor<T> createCursor(ContentTable<T> table);
	
	/** creates a new cursor, but using a key so that it is possible to retrieve it later
	 * 
	 * @param table
	 * @param key
	 * @return
	 */
	<T> Cursor<T> createCursor(Class<T> table, Object key);
	<T> void dropCursor(Object key);
	
	
	//<T> void dropSingletonProperty(Class<T> type, String name);
	//<T> SingletonProperty<T> createSingletonProperty(Class<T> type, String name);
	

}
