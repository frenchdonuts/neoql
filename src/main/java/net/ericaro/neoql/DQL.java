package net.ericaro.neoql;


/** The Data Query Language Interface
 * 
 * @author eric
 *
 */
public interface DQL {
// TODO provide simple find methods here, inspire by what I can do using NeoQL static methods
	
	
//	Iterable<SingletonProperty> getSingletons();            
	Iterable<ContentTable> getTables();                     
	Iterable<Cursor> getCursors();            
	                                                        
	<T> ContentTable<T> getTable(Class<T> table);
	<T> Cursor<T> getCursor(Object key);
	
	
	
}
