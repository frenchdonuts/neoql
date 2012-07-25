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

	//<T> void dropTable(Class<T> table);
	<T> ContentTable<T> createTable(Class<T> table, Column<T, ?>... columns);
	<T> Cursor<T> createCursor(Table<T> table);
	//<T> void dropSingletonProperty(Class<T> type, String name);
	//<T> SingletonProperty<T> createSingletonProperty(Class<T> type, String name);
	

}
