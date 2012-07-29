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
	<T> ContentTable<T> getTable(Class<T> table);
	
	
	
}
