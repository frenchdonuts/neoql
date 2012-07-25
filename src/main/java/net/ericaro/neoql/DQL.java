package net.ericaro.neoql;

import net.ericaro.neoql.properties.SingletonProperty;

/** The Data Query Language Interface
 * 
 * @author eric
 *
 */
public interface DQL {
// TODO provide simple find methods here, inspire by what I can do using NeoQL static methods
	
	
//	Iterable<SingletonProperty> getSingletons();            
	Iterable<ContentTable> getTables();                     
	Iterable<Cursor> getRows();            
	                                                        
	<T> ContentTable<T> getTable(Class<T> table);
	
	
	
}
